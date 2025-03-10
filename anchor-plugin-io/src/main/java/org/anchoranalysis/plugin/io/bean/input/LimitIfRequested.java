/*-
 * #%L
 * anchor-plugin-io
 * %%
 * Copyright (C) 2010 - 2021 Owen Feehan, ETH Zurich, University of Zurich, Hoffmann-La Roche
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

import io.vavr.control.Either;
import java.util.Optional;
import org.anchoranalysis.io.input.InputContextParameters;
import org.anchoranalysis.io.input.InputFromManager;
import org.anchoranalysis.io.input.InputReadFailedException;
import org.anchoranalysis.io.input.InputsWithDirectory;
import org.anchoranalysis.io.input.bean.InputManagerParameters;
import org.anchoranalysis.io.input.bean.InputManagerUnary;

/**
 * Like {@link Limit} if it is requested in the {@link InputContextParameters}, makes no change to
 * the inputs.
 *
 * @author Owen Feehan
 * @param <T> input-object type
 */
public class LimitIfRequested<T extends InputFromManager> extends InputManagerUnary<T> {

    @Override
    protected InputsWithDirectory<T> inputsFromDelegate(
            InputsWithDirectory<T> fromDelegate, InputManagerParameters parameters)
            throws InputReadFailedException {

        int totalNumberInputs = fromDelegate.inputs().size();
        Optional<Integer> option =
                parameters
                        .getInputContext()
                        .getLimitUpper()
                        .map(either -> calculateLimit(either, totalNumberInputs));
        if (option.isPresent()) {
            LimitHelper.limitInputsIfNecessary(
                    fromDelegate.listIterator(), option.get(), totalNumberInputs, parameters);
        }
        return fromDelegate;
    }

    /** Calculates the number of inputs to use as a limit. */
    private static int calculateLimit(Either<Integer, Double> limitUpper, int totalNumberInputs) {
        if (limitUpper.isLeft()) {
            return limitUpper.getLeft();
        } else {
            return (int) Math.round(limitUpper.get() * totalNumberInputs);
        }
    }
}
