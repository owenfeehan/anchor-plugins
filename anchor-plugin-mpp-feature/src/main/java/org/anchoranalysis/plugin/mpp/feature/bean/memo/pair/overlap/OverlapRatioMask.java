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
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.shared.relation.EqualToBean;
import org.anchoranalysis.bean.shared.relation.RelationBean;
import org.anchoranalysis.feature.cache.SessionInput;
import org.anchoranalysis.feature.calculate.FeatureCalculationException;
import org.anchoranalysis.mpp.feature.input.memo.FeatureInputPairMemo;
import org.anchoranalysis.mpp.mark.voxelized.memo.VoxelizedMarkMemo;

public class OverlapRatioMask extends OverlapMaskBase {

    // START BEAN PROPERTIES
    @BeanField @Getter @Setter private boolean useMax = false;
    // END BEAN PROPERTIES

    private RelationBean relationToThreshold = new EqualToBean();

    @Override
    public double calculate(SessionInput<FeatureInputPairMemo> input)
            throws FeatureCalculationException {

        FeatureInputPairMemo inputSessionless = input.get();

        double overlap = overlapWithGlobalMask(input);

        return overlapRatioToggle(
                inputSessionless.getObj1(), inputSessionless.getObj2(), overlap, getRegionID());
    }

    private double overlapRatioToggle(
            VoxelizedMarkMemo obj1, VoxelizedMarkMemo obj2, double overlap, int regionID) {

        if (overlap == 0.0) {
            return 0.0;
        }

        double volume =
                volumeAgg(
                        obj1,
                        obj2,
                        regionID,
                        relationToThreshold,
                        OverlapRatioUtilities.maxOrMin(useMax));
        return overlap / volume;
    }
}
