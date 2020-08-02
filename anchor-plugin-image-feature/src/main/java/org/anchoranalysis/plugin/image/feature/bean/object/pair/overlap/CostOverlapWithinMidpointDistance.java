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

package org.anchoranalysis.plugin.image.feature.bean.object.pair.overlap;

import java.util.Optional;
import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.core.geometry.Point3d;
import org.anchoranalysis.feature.cache.SessionInput;
import org.anchoranalysis.feature.calc.FeatureCalculationException;
import org.anchoranalysis.image.bean.unitvalue.distance.UnitValueDistance;
import org.anchoranalysis.image.extent.ImageResolution;
import org.anchoranalysis.image.feature.bean.object.pair.FeaturePairObjects;
import org.anchoranalysis.image.feature.object.input.FeatureInputPairObjects;
import org.anchoranalysis.image.orientation.DirectionVector;

/**
 * TODO the center-of-gravity calculation can be turned into a FeatureCalculation which is cacheable
 *
 * @author Owen Feehan
 */
public class CostOverlapWithinMidpointDistance extends FeaturePairObjects {

    // START BEAN PROPERTIES
    @BeanField @Getter @Setter private UnitValueDistance maxDistance;

    @BeanField @Getter @Setter private double minOverlap = 0.6;

    @BeanField @Getter @Setter private boolean suppressZ = true;
    // END BEAN PROPERTIES

    @Override
    public double calc(SessionInput<FeatureInputPairObjects> input)
            throws FeatureCalculationException {

        FeatureInputPairObjects inputSessionless = input.get();

        if (isDistanceMoreThanMax(inputSessionless)) {
            return 1.0;
        }

        double overlapRatio = OverlapRatioUtilities.overlapRatioToMaxVolume(inputSessionless);

        if (overlapRatio > minOverlap) {
            return 1.0 - overlapRatio;
        } else {
            return 1.0;
        }
    }

    private boolean isDistanceMoreThanMax(FeatureInputPairObjects params)
            throws FeatureCalculationException {

        if (!params.getResOptional().isPresent()) {
            throw new FeatureCalculationException(
                    "This feature requires an Image-Res in the input");
        }

        Point3d cog1 = params.getFirst().centerOfGravity();
        Point3d cog2 = params.getSecond().centerOfGravity();

        double distance = calcDistance(cog1, cog2);
        try {
            return distance > calcMaxDistance(cog1, cog2, params.getResOptional());
        } catch (OperationFailedException e) {
            throw new FeatureCalculationException(e);
        }
    }

    private double calcDistance(Point3d cog1, Point3d cog2) {
        if (suppressZ) {
            cog1.setZ(0);
            cog2.setZ(0);
        }
        return cog1.distance(cog2);
    }

    // We measure the euclidian distance between center-points
    private double calcMaxDistance(Point3d cog1, Point3d cog2, Optional<ImageResolution> res)
            throws OperationFailedException {
        DirectionVector vec = DirectionVector.createBetweenTwoPoints(cog1, cog2);
        return maxDistance.resolve(res, vec);
    }
}
