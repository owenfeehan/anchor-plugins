/*-
 * #%L
 * anchor-plugin-mpp-feature
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

package org.anchoranalysis.plugin.mpp.feature.bean.memo.pair.overlap;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.feature.calculate.FeatureCalculationException;
import org.anchoranalysis.feature.calculate.FeatureCalculationInput;
import org.anchoranalysis.mpp.feature.input.FeatureInputPairMemo;
import org.anchoranalysis.plugin.mpp.feature.bean.memo.pair.FeaturePairMemoSingleRegion;
import org.anchoranalysis.plugin.mpp.feature.overlap.CalculateOverlap;

@NoArgsConstructor
public class MaxOverlapRatio extends FeaturePairMemoSingleRegion {

    // START BEAN PROPERTIES
    @BeanField @Getter @Setter private double max = -1;

    @BeanField @Getter @Setter private double penaltyValue = -10;

    @BeanField @Getter @Setter private boolean includeShell = false;
    // END BEAN PROPERTIES

    public MaxOverlapRatio(double max) {
        this();
        this.max = max;
    }

    @Override
    public String describeParameters() {
        return String.format("max=%f", max);
    }

    @Override
    public double calculate(FeatureCalculationInput<FeatureInputPairMemo> input)
            throws FeatureCalculationException {

        FeatureInputPairMemo inputPair = input.get();

        double ratio =
                OverlapRatioUtilities.overlapRatio(
                        inputPair.getObject1(),
                        inputPair.getObject2(),
                        input.calculate(new CalculateOverlap(getRegionID())),
                        getRegionID(),
                        false,
                        Math::min);

        if (ratio > max) {
            return penaltyValue;
        } else {
            return 0;
        }
    }
}
