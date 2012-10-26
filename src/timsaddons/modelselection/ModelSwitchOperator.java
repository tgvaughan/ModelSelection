/*
 * Copyright (C) 2012 Tim Vaughan <tgvaughan@gmail.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package timsaddons.modelselection;

import beast.core.Description;
import beast.core.Distribution;
import beast.core.Input;
import beast.core.Input.Validate;
import beast.core.Operator;
import beast.core.parameter.IntegerParameter;
import beast.core.parameter.RealParameter;
import beast.util.Randomizer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

    
/**
 * @author Tim Vaughan <tgvaughan@gmail.com>
 */
@Description("Operator to switch between models.")
public class ModelSwitchOperator extends Operator {

    public Input<IntegerParameter> modelNumberInput =
            new Input<IntegerParameter> ("modelNumber",
            "Model selector.  Used to select set of parameters available for "
            + "modification.", Validate.REQUIRED);
    
    public Input<List<RealParameter>> parametersInput =
            new Input<List<RealParameter>>("parameter",
            "Parameter for operator to scale.",
            new ArrayList<RealParameter>(), Validate.REQUIRED);
    
    public Input<IntegerParameter> parameterModelIndicesInput =
            new Input<IntegerParameter>("parameterModelIndices",
            "Indecies of models to which each parameter belongs.",
            Validate.REQUIRED);
    
    public Input<List<Distribution>> priorDistributionsInput =
            new Input<List<Distribution>>("prior",
            "Prior distribution to select new parameter from",
            new ArrayList<Distribution>(), Validate.REQUIRED);
    
    Map<Integer, List<RealParameter>> parameterLists;
    Map<Integer, List<Distribution>> priorLists;
    
    @Override
    public void initAndValidate() throws Exception {
        
        if (parametersInput.get().size() != parameterModelIndicesInput.get().getDimension())
            throw new IllegalArgumentException("Number of parameters does not "
                    + "match number of parameter set indices.");
        
        // Check size of scale factor array provided:
        if (priorDistributionsInput.get().size() != parametersInput.get().size())
            throw new IllegalArgumentException("Number of priors does not"
                    + " match number of parameters provided.");
        
        // Sort parameters and scale factors into distinct groups according to index list.
        
        parameterLists = new HashMap<Integer,List<RealParameter>>();
        priorLists = new HashMap<Integer, List<Distribution>>();
        
        for (int i=0; i<parameterModelIndicesInput.get().getDimension(); i++) {
            int idx = parameterModelIndicesInput.get().getValue(i);
            
            if (!parameterLists.containsKey(idx)) {
                parameterLists.put(idx, new ArrayList<RealParameter>());
                priorLists.put(idx, new ArrayList<Distribution>());
            }
            
            parameterLists.get(idx).add(parametersInput.get().get(i));
            priorLists.get(idx).add(priorDistributionsInput.get().get(i));
        }

        // Tell BEAST that we don't want the scale factors and model indices
        // to form part of the state
        
        parameterModelIndicesInput.get().m_bIsEstimated.setValue(false, parameterModelIndicesInput.get());
    }

    @Override
    public double proposal() {
        // Choose parameter to modify:
        int m = modelNumberInput.get().getValue();        
        int nParams = parameterLists.get(m).size();        
        int i = Randomizer.nextInt(nParams);
        RealParameter param = parameterLists.get(m).get(i);
        
        // Choose scale factor:
        double fmax = scaleFactors.get(m).get(i);
        double f = 1.0/fmax + Randomizer.nextDouble()*(fmax-1.0/fmax);
        
        // Record old parameter value for HR calculation:
        double oldVal = param.getValue();
        
        // Use scale factor to propose new parameter value:
        double newVal = oldVal*f;
        
        // Update StateNode:
        param.setValue(newVal);
        
        // Return Hastings ratio:
        return Math.log(oldVal/newVal);
    }
}