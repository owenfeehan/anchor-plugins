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

package org.anchoranalysis.plugin.image.feature.bean.object.single.shape;

import cern.colt.list.DoubleArrayList;
import cern.jet.stat.Descriptive;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.geometry.Point3d;
import org.anchoranalysis.core.geometry.Point3i;
import org.anchoranalysis.feature.cache.SessionInput;
import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.image.feature.bean.object.single.FeatureSingleObject;
import org.anchoranalysis.image.feature.object.input.FeatureInputSingleObject;
import org.anchoranalysis.image.object.ObjectMask;
import org.anchoranalysis.image.outline.FindOutline;
import org.anchoranalysis.image.points.PointsFromBinaryVoxelBox;

// Standard deviation of distance from surface voxels to centroid
public class ObjectRadiusStandardDeviation extends FeatureSingleObject {

    // START BEAN PROPERTIES
    @BeanField @Getter @Setter
    private boolean cov =
            false; // Returns the coefficient of variation (stdDev/mean) instead of stdDev
    // END BEAN PROPERTIES

    @Override
    public double calc(SessionInput<FeatureInputSingleObject> input) throws FeatureCalcException {

        ObjectMask object = input.get().getObject();

        // Get the outline
        List<Point3i> pointsOutline = createMaskOutlineAsPoints(object, 1);

        // Distances from the center to each point on the outline
        DoubleArrayList distances = distancesToPoints(object.centerOfGravity(), pointsOutline);

        return calcStatistic(distances);
    }

    private static DoubleArrayList distancesToPoints(Point3d pointFrom, List<Point3i> pointsTo) {
        DoubleArrayList distances = new DoubleArrayList(pointsTo.size());
        for (Point3i point : pointsTo) {

            Point3d shifted =
                    new Point3d(point.getX() + 0.5, point.getY() + 0.5, point.getZ() + 0.5);

            distances.add(pointFrom.distance(shifted));
        }
        return distances;
    }

    private double calcStatistic(DoubleArrayList distances) {
        // Calculate Std Deviation
        int size = distances.size();
        double sum = Descriptive.sum(distances);

        double sumOfSquares = Descriptive.sumOfSquares(distances);
        double variance = Descriptive.variance(size, sum, sumOfSquares);
        double stdDev = Descriptive.standardDeviation(variance);

        if (cov) {
            double mean = sum / size;
            return stdDev / mean;
        } else {
            return stdDev;
        }
    }

    private static List<Point3i> createMaskOutlineAsPoints(ObjectMask mask, int numberErosions) {

        List<Point3i> pointsOutline = new ArrayList<>();

        ObjectMask outline = FindOutline.outline(mask, numberErosions, false, true);
        PointsFromBinaryVoxelBox.addPointsFromVoxelBox3D(
                outline.binaryVoxelBox(), outline.getBoundingBox().cornerMin(), pointsOutline);

        return pointsOutline;
    }
}
