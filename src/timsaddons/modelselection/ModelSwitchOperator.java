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
import beast.core.Input;
import beast.core.Input.Validate;
import beast.core.Operator;
import beast.core.parameter.IntegerParameter;
import beast.core.parameter.RealParameter;
import beast.math.distributions.Gamma;
import beast.util.Randomizer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.math.distribution.GammaDistribution;
import org.apache.commons.math.distribution.GammaDistributionImpl;

    
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
    
    public Input<RealParameter> proposalAlphaInput =
            new Input<RealParameter>("proposalAlpha",
            "Alpha (shape) parameters of proposal gamma distribution for each parameter.",
            Validate.REQUIRED);
    
    public Input<RealParameter> proposalLambdaInput =
            new Input<RealParameter>("proposalLambda",
            "Lambda (rate) parameters of proposal gamma distribution for each parameter.",
            Validate.REQUIRED);
    
    Map<Integer, List<RealParameter>> parameterLists;
    Map<Integer, List<Double>> proposalAlphaLists;
    Map<Integer, List<Double>> proposalLambdaLists;
    
    int nModels;
    
    @Override
    public void initAndValidate() throws Exception {
        
        if (parametersInput.get().size() != parameterModelIndicesInput.get().getDimension())
            throw new IllegalArgumentException("Number of parameters does not "
                    + "match number of parameter set indices.");
        
        // Check size of scale factor array provided:
        int nParameters = parametersInput.get().size();
        if ((proposalAlphaInput.get().getDimension() != nParameters)
                || (proposalLambdaInput.get().getDimension() != nParameters))
            throw new IllegalArgumentException("Number of proposal alpha or "
                    + "lambda parameters does not match number of model "
                    + "parameters provided.");
        
        // Sort parameters and scale factors into distinct groups according to index list.
        
        parameterLists = new HashMap<Integer,List<RealParameter>>();
        proposalAlphaLists = new HashMap<Integer, List<Double>>();
        proposalLambdaLists = new HashMap<Integer, List<Double>>();
        
        for (int i=0; i<parameterModelIndicesInput.get().getDimension(); i++) {
            int idx = parameterModelIndicesInput.get().getValue(i);
            
            if (!parameterLists.containsKey(idx)) {
                parameterLists.put(idx, new ArrayList<RealParameter>());
                proposalAlphaLists.put(idx, new ArrayList<Double>());
                proposalLambdaLists.put(idx, new ArrayList<Double>());
            }
            
            parameterLists.get(idx).add(parametersInput.get().get(i));
            proposalAlphaLists.get(idx).add(proposalAlphaInput.get().getArrayValue(i));
            proposalLambdaLists.get(idx).add(proposalLambdaInput.get().getArrayValue(i));
        }
        
        // Record distinct number of models seen:
        nModels = parameterLists.size();
        
        if (nModels<2) {
            throw new IllegalArgumentException("ModelSwitchOperator needs at "
                    + "least 2 distinct models to switch between.");
        }

        // Tell BEAST that we don't want the scale factors and model indices
        // to form part of the state
        
        parameterModelIndicesInput.get().m_bIsEstimated.setValue(false, parameterModelIndicesInput.get());
    }

    @Override
    public double proposal() {
        
        // Uniformly select new model from available models:
        int oldModel = modelNumberInput.get().getValue();
        int newModel;
        do {
            newModel = Randomizer.nextInt(nModels);
        } while (newModel == oldModel);

        // Update model number
        modelNumberInput.get().setValue(newModel);
        
        // Record selection probability of old parameters in HR:
        double logHR = 0;
        for (int i=0; i<parameterLists.get(oldModel).size(); i++) {
            double alpha = proposalAlphaLists.get(oldModel).get(i);
            double lambda = proposalLambdaLists.get(oldModel).get(i);
            double x = parameterLists.get(oldModel).get(i).getValue();
            logHR += (new GammaDistributionImpl(alpha, lambda)).density(x);
        }
        
        // Select new parameters and incorporate selection probability into HR:
        for (int i=0; i<parameterLists.get(newModel).size(); i++) {
            double alpha = proposalAlphaLists.get(newModel).get(i);
            double lambda = proposalLambdaLists.get(newModel).get(i);
            double xprime = Randomizer.nextGamma(alpha, lambda);
            logHR -= (new GammaDistributionImpl(alpha, lambda)).density(xprime);
            
            parameterLists.get(newModel).get(i).setValue(xprime);
        }
        
        return logHR;
    }
}