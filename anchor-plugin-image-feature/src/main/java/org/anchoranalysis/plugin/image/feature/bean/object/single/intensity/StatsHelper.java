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

package org.anchoranalysis.plugin.image.feature.bean.object.single.intensity;

import java.util.function.ToDoubleFunction;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.image.channel.Channel;
import org.anchoranalysis.image.histogram.Histogram;
import org.anchoranalysis.image.histogram.HistogramFactory;
import org.anchoranalysis.image.object.ObjectMask;
import org.anchoranalysis.image.voxel.statistics.VoxelStatisticsFromHistogram;
import org.anchoranalysis.plugin.image.intensity.IntensityMeanCalculator;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
class StatsHelper {

    /**
     * Calculates the mean-intensity of a masked-part of each slice, and returns the maximum value
     * across all sices
     *
     * @param chnl
     * @param object
     * @param excludeZero
     * @return
     * @throws FeatureCalcException
     */
    public static ValueAndIndex calcMaxSliceMean(
            Channel chnl, ObjectMask object, boolean excludeZero) throws FeatureCalcException {

        double max = Double.NEGATIVE_INFINITY;
        int index = -1;

        for (int z = 0; z < object.getBoundingBox().extent().getZ(); z++) {

            ObjectMask slice = object.extractSlice(z, true);

            // We adjust the z coordiante to point to the channel
            int zTarget =
                    slice.getBoundingBox().cornerMin().getZ()
                            + object.getBoundingBox().cornerMin().getZ();
            slice = slice.mapBoundingBox(bbox -> bbox.shiftToZ(zTarget));

            if (slice.hasPixelsGreaterThan(0)) {
                double mean =
                        IntensityMeanCalculator.calcMeanIntensityObject(chnl, slice, excludeZero);

                if (mean > max) {
                    index = z;
                    max = mean;
                }
            }
        }

        return new ValueAndIndex(max, index);
    }

    /**
     * Calculates the mean-intensity of a certain number of (highest or lowest-intesntiy) pixels
     * from the masked part of a chanel
     *
     * <p>This number of pixels can either taken from the highest or lowest part of the histogram
     *
     * @param chnl
     * @param object
     * @param numPixels the number of pixels to be considered (either the highest-intensity pixels,
     *     or lowest-intensity pixel)
     * @param highest iff TRUE the highest-intensity pixels are used in the mask, otherwise the
     *     lowest-intensity pixels are used
     * @return
     * @throws OperationFailedException
     */
    public static double calcMeanNumPixels(
            Channel chnl, ObjectMask object, int numPixels, boolean highest)
            throws OperationFailedException {

        Histogram histogram = HistogramFactory.create(chnl, object);

        Histogram hCut =
                highest
                        ? histogram.extractPixelsFromRight(numPixels)
                        : histogram.extractPixelsFromLeft(numPixels);

        return hCut.mean();
    }

    public static double calcStatistic(
            Channel chnl,
            ObjectMask object,
            boolean ignoreZero,
            double emptyValue,
            ToDoubleFunction<VoxelStatisticsFromHistogram> funcExtractStatistic) {
        Histogram hist = HistogramFactory.createHistogramIgnoreZero(chnl, object, ignoreZero);

        if (hist.getTotalCount() == 0) {
            return emptyValue;
        }

        return funcExtractStatistic.applyAsDouble(new VoxelStatisticsFromHistogram(hist));
    }
}
