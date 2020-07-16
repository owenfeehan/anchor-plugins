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
/* (C)2020 */
package org.anchoranalysis.plugin.image.feature.bean.object.pair;

import java.util.Optional;
import lombok.EqualsAndHashCode;
import org.anchoranalysis.feature.cache.calculation.ResolvedCalculation;
import org.anchoranalysis.image.feature.object.input.FeatureInputPairObjects;
import org.anchoranalysis.image.feature.object.input.FeatureInputSingleObject;
import org.anchoranalysis.image.object.ObjectMask;
import org.anchoranalysis.plugin.image.feature.object.calculation.delegate.CalculateInputFromDelegateOption;

@EqualsAndHashCode(callSuper = true)
class CalculateIntersectionInput
        extends CalculateInputFromDelegateOption<
                FeatureInputSingleObject, FeatureInputPairObjects, Optional<ObjectMask>> {
    public CalculateIntersectionInput(
            ResolvedCalculation<Optional<ObjectMask>, FeatureInputPairObjects> ccIntersection) {
        super(ccIntersection);
    }

    @Override
    protected Optional<FeatureInputSingleObject> deriveFromDelegate(
            FeatureInputPairObjects input, Optional<ObjectMask> delegate) {
        if (!delegate.isPresent()) {
            return Optional.empty();
        }

        assert (delegate.get().hasPixelsGreaterThan(0));
        assert (delegate.get() != null);

        return Optional.of(
                new FeatureInputSingleObject(delegate.get(), input.getNrgStackOptional()));
    }
}
