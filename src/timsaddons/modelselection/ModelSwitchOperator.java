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
import beast.util.Randomizer;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
            "Model parameter - will be modified when corresp. model is chosen.",
            new ArrayList<RealParameter>(), Validate.REQUIRED);
    
    public Input<IntegerParameter> parameterModelIndicesInput =
            new Input<IntegerParameter>("parameterModelIndices",
            "Indecies of models to which each parameter belongs.",
            Validate.REQUIRED);
    
    public Input<RealParameter> proposalShapesInput =
            new Input<RealParameter>("proposalShapes",
            "Shape parameters of proposal gamma distribution for each parameter.",
            Validate.REQUIRED);
    
    public Input<RealParameter> proposalMeansInput =
            new Input<RealParameter>("proposalMeans",
            "Mean of proposal gamma distribution for each parameter.",
            Validate.REQUIRED);
    
    int nModels;
    
    @Override
    public void initAndValidate() throws Exception {
        
        if (parametersInput.get().size() != parameterModelIndicesInput.get().getDimension())
            throw new IllegalArgumentException("Number of parameters does not "
                    + "match number of parameter set indices.");
        
        // Check size of scale factor array provided:
        int nParameters = parametersInput.get().size();
        if ((proposalShapesInput.get().getDimension() != nParameters)
                || (proposalMeansInput.get().getDimension() != nParameters))
            throw new IllegalArgumentException("Number of proposal shape or "
                    + "mean parameters does not match number of model "
                    + "parameters provided.");
        
        
        // Record distinct number of model indices:
        Set<Integer> modelIndices = new HashSet<Integer>();
        for (int idx : parameterModelIndicesInput.get().getValues()) {
            modelIndices.add(idx);
        }
        nModels = modelIndices.size();
        
        if (nModels<2) {
            throw new IllegalArgumentException("ModelSwitchOperator needs at "
                    + "least 2 distinct models to switch between.");
        }

        // Tell BEAST that we don't want the scale factors and model indices
        // to form part of the state
        
        parameterModelIndicesInput.get().isEstimatedInput.setValue(false, parameterModelIndicesInput.get());
        proposalShapesInput.get().isEstimatedInput.setValue(false, proposalShapesInput.get());
        proposalMeansInput.get().isEstimatedInput.setValue(false, proposalMeansInput.get());
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
        
        for (int i=0; i<parameterModelIndicesInput.get().getDimension(); i++) {
            int m = parameterModelIndicesInput.get().getValue(i);
            
            if (m==oldModel) {
                double shape = proposalShapesInput.get().getValue(i);
                double scale = proposalMeansInput.get().getValue(i)/shape;
                double x = parametersInput.get().get(i).getValue();
                
                logHR += (new GammaDistributionImpl(shape, scale)).density(x);
            }
            
            if (m==newModel) {
                double shape = proposalShapesInput.get().getValue(i);
                double scale = proposalMeansInput.get().getValue(i)/shape;
                double xprime = Randomizer.nextGamma(shape, 1.0/scale);
                
                logHR -= (new GammaDistributionImpl(shape, scale)).density(xprime);
                
                parametersInput.get().get(i).setValue(xprime);
            }
        }
        
        return logHR;
    }
    
    /**
     * Main for debugging.
     * 
     * @param args 
     */
    public static void main (String[] args) throws FileNotFoundException {
        
        // Check that RNG is working as expected
        PrintStream outf = new PrintStream("rngtest.txt");
        for (int i=0; i<10000; i++) {
            outf.println(Randomizer.nextGamma(2.0, 3.0));
        }
        outf.close();
       
        // Check that Gamma distribution PDF is behaving as expected:
        outf = new PrintStream("pdftest.txt");
        outf.println("x p");
        for (double x=0; x<10; x+=0.01) {
            outf.println(x + " " + (new GammaDistributionImpl(2.0,1.0/3.0)).density(x));
        }
        outf.close();
    }
}