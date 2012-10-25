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
package beast.evolution.operators;

import beast.core.Description;
import beast.core.Input;
import beast.core.Input.Validate;
import beast.core.Operator;
import beast.core.parameter.IntegerParameter;
import beast.core.parameter.RealParameter;
import beast.util.Randomizer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

    
/**
 * @author Tim Vaughan <tgvaughan@gmail.com>
 */
@Description("Scales a real parameter randomly selected from a set "
        + "of such parameters.  The set is selected from a list of "
        + "such sets conditional on the value of an integer parameter. "
        + "Useful for model selection calculations.")
public class ConditionalScaleOperator extends Operator {

    public Input<Integer> modelNumberInput = new Input<Integer> ("modelNumber",
            "Model selector.  Used to select set of parameters available for "
            + "modification.", Validate.REQUIRED);
    
    public Input<List<RealParameter>> parametersInput = new Input<List<RealParameter>>(
            "parameter", "Parameter for operator to scale.", Validate.REQUIRED);
    
    public Input<IntegerParameter> parameterSetsInput = new Input<IntegerParameter>(
            "parameterSets", "List of indicies specifying set to which each parameter belongs.",
            Validate.REQUIRED);
    
    public Input<RealParameter> scaleFactorInput = new Input<RealParameter>(
            "scaleFactor", "Maximum scale factor used to generate proposal.",
            Validate.REQUIRED);
    
    Map<Integer, List<RealParameter>> parameterLists;
    Map<Integer, List<Double>> scaleFactors;
    
    @Override
    public void initAndValidate() {
        
        if (parametersInput.get().size() != parameterSetsInput.get().getDimension())
            throw new IllegalArgumentException("Number of parameters does not "
                    + "match number of parameter set indices.");
        
        // Check size of scale factor array provided:
        if (scaleFactorInput.get().getDimension()>1 &&
                scaleFactorInput.get().getDimension() != parametersInput.get().size())
            throw new IllegalArgumentException("Number of scale factors is not"
                    + " 1 and does not match number of parameters provided.");
        
        // Sort parameters  and scale factors into distinct groups according to index list.
        for (int i=0; i<parameterSetsInput.get().getDimension(); i++) {
            int idx = parameterSetsInput.get().getValue(i);
            
            if (!parameterLists.containsKey(idx)) {
                parameterLists.put(idx, new ArrayList<RealParameter>());
                scaleFactors.put(idx, new ArrayList<Double>());
            }
            
            parameterLists.get(idx).add(parametersInput.get().get(i));
            if (scaleFactorInput.get().getDimension()>1)
                scaleFactors.get(idx).add(scaleFactorInput.get().getArrayValue(i));
            else
                scaleFactors.get(idx).add(scaleFactorInput.get().getValue());
        }
        

    }

    @Override
    public double proposal() {
        double logHR = 0.0;

        // Choose parameter to modify:
        int m = modelNumberInput.get();        
        int nParams = parameterLists.get(m).size();        
        int i = Randomizer.nextInt(nParams);
        RealParameter param = parameterLists.get(m).get(i);
        
        // Choose scale factor:
        double fmax = scaleFactors.get(m).get(i);
        double f = 1.0/fmax + Randomizer.nextDouble()*(fmax-1.0/fmax);
        
        return logHR;
    }    
}
