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
import beast.core.State;
import beast.core.parameter.IntegerParameter;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * @author Tim Vaughan <tgvaughan@gmail.com>
 */
@Description("Switch between different distributions based on value of "
        + "an IntegerParameter.")
public class DistributionSwitcher extends Distribution {
    
    public Input<IntegerParameter> modelNumberInput =
            new Input<IntegerParameter>("modelNumber",
            "Number specifying distribution currently in use.",
            Validate.REQUIRED);
    
    public Input<List<Distribution>> distributionsInput =
            new Input<List<Distribution>>("distribution",
            "Distribution to select using modelNumber.",
            new ArrayList<Distribution>(),
            Validate.REQUIRED);
    
    int nDistribs;
    
    boolean distribsInitialised;
    
    @Override
    public void initAndValidate() throws Exception {
        nDistribs = distributionsInput.get().size();
        modelNumberInput.get().setLower(0);
        modelNumberInput.get().setUpper(nDistribs-1);
        
        distribsInitialised = false;
    }
    
    @Override
    public double calculateLogP() throws Exception {
        
        if (!distribsInitialised) {
            for (int m=0; m<nDistribs; m++) {
                if (m==modelNumberInput.get().getValue())
                    logP = distributionsInput.get().get(m).calculateLogP();
                else
                    distributionsInput.get().get(m).calculateLogP();
            }            
            distribsInitialised = true;
        } else {            
            int m = modelNumberInput.get().getValue();
            logP = distributionsInput.get().get(m).calculateLogP();
        }
        
        return logP;
    }

    @Override
    public List<String> getArguments() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public List<String> getConditions() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void sample(State state, Random random) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
}
