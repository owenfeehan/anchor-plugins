/*-
 * #%L
 * anchor-plugin-image-feature
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

package org.anchoranalysis.plugin.image.feature.object.calculation.delegate;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import org.anchoranalysis.feature.calculate.FeatureCalculation;
import org.anchoranalysis.feature.calculate.FeatureCalculationException;
import org.anchoranalysis.feature.calculate.cache.CalculationResolver;
import org.anchoranalysis.feature.calculate.cache.ResolvedCalculation;
import org.anchoranalysis.feature.input.FeatureInput;

/**
 * A base class for Cached-Calculations that generate a Params for feature-calculation using an
 * existing "delegate" calculation
 *
 * <p>These types of calculations involve two steps:
 *
 * <ul>
 *   <li>Calculating from an existing cached-calculation
 *   <li>Applying a transform to generate parameters
 * </ul>
 *
 * @author Owen Feehan
 * @param <S> final-type of CachedCalculation
 * @param <T> feature input-type as input to cached-calculations
 * @param <U> delegate-type of CachedCalculation
 */
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode(callSuper = false)
public abstract class CalculateInputFromDelegate<S, T extends FeatureInput, U>
        extends FeatureCalculation<S, T> {

    private final ResolvedCalculation<U, T> ccDelegate;

    protected CalculateInputFromDelegate(
            FeatureCalculation<U, T> ccDelegate, CalculationResolver<T> cache) {
        this(cache.search(ccDelegate));
    }

    @Override
    public S execute(T input) throws FeatureCalculationException {

        U delegate = ccDelegate.getOrCalculate(input);
        return deriveFromDelegate(input, delegate);
    }

    protected abstract S deriveFromDelegate(T input, U delegate);

    protected ResolvedCalculation<U, T> getDelegate() {
        return ccDelegate;
    }
}
