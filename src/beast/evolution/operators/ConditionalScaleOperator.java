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
import beast.core.parameter.RealParameter;
import java.util.List;

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
    
    @Override
    public double proposal() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
}
