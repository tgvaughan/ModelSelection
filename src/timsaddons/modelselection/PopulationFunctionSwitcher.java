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
import beast.evolution.tree.coalescent.PopulationFunction;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Special population function which can be used switch between distinct
 * demographic models during a chain.
 * @author Tim Vaughan <tgvaughan@gmail.com>
 */
@Description("Switch between different demographic models by altering an "
        + "integer parameter")
public class PopulationFunctionSwitcher extends PopulationFunction.Abstract {
    
    public Input<Integer> modelNumberInput = new Input<Integer>("modelNumber",
            "Inter specifying demographic model currently in use.",
            Validate.REQUIRED);
    
    public Input<List<PopulationFunction>> populationFunctionsInput
            = new Input<List<PopulationFunction>>("populationFunction",
            "One or more population functions to switch between.",
            Validate.REQUIRED);

    // Number of models in switcher
    int nModels;
    
    @Override
    public void initAndValidate() {
        nModels = populationFunctionsInput.get().size();
    }
    
    @Override
    public List<String> getParameterIds() {
        List<String> idList = new ArrayList<String>();
        for (int m=0; m<nModels; m++)
            idList.addAll(populationFunctionsInput.get().get(m).getParameterIds());

        return Collections.unmodifiableList(idList);
    }

    @Override
    public double getPopSize(double t) {
        int m = modelNumberInput.get();
        return populationFunctionsInput.get().get(m).getPopSize(t);
    }

    @Override
    public double getIntensity(double t) {
        int m = modelNumberInput.get();
        return populationFunctionsInput.get().get(m).getIntensity(t);
    }

    @Override
    public double getInverseIntensity(double t) {
        int m = modelNumberInput.get();
        return populationFunctionsInput.get().get(m).getInverseIntensity(t);
    }
    
}
