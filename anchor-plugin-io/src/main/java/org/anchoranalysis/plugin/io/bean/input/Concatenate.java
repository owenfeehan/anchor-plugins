/*-
 * #%L
 * anchor-plugin-io
 * %%
 * Copyright (C) 2010 - 2020 Owen Feehan, ETH Zurich, University of Zurich, Hoffmann-La Roche
 * %%
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 * #L%
 */

package org.anchoranalysis.plugin.io.bean.input;

import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.progress.ProgressMultiple;
import org.anchoranalysis.io.input.InputFromManager;
import org.anchoranalysis.io.input.InputReadFailedException;
import org.anchoranalysis.io.input.InputsWithDirectory;
import org.anchoranalysis.io.input.bean.InputManager;
import org.anchoranalysis.io.input.bean.InputManagerParameters;

/**
 * Concatenates the inputs from multiple {@link InputManager}s that all provide the same input-type.
 *
 * @author Owen Feehan
 * @param <T> input type
 */
public class Concatenate<T extends InputFromManager> extends InputManager<T> {

    // START BEAN PROPERTIES
    /** The list of {@link InputManager}s that will be concatenated. */
    @BeanField @Getter @Setter private List<InputManager<T>> list = new ArrayList<>();
    // END BEAN PROPERTIES

    @Override
    public InputsWithDirectory<T> inputs(InputManagerParameters parameters)
            throws InputReadFailedException {

        try (ProgressMultiple progressMultiple =
                new ProgressMultiple(parameters.getProgress(), list.size())) {

            ArrayList<T> out = new ArrayList<>();

            for (InputManager<T> inputManager : list) {
                out.addAll(inputManager.inputs(parameters).inputs());

                progressMultiple.incrementChild();
            }
            return new InputsWithDirectory<>(out);
        }
    }
}
