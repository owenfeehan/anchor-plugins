/*-
 * #%L
 * anchor-plugin-mpp
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

package org.anchoranalysis.plugin.mpp.bean.outline.visitscheduler;

import java.nio.ByteBuffer;
import java.util.Optional;
import org.anchoranalysis.core.error.InitException;
import org.anchoranalysis.core.geometry.Point3d;
import org.anchoranalysis.core.geometry.Point3i;
import org.anchoranalysis.core.geometry.PointConverter;
import org.anchoranalysis.core.geometry.Tuple3i;
import org.anchoranalysis.core.random.RandomNumberGenerator;
import org.anchoranalysis.image.binary.values.BinaryValues;
import org.anchoranalysis.image.binary.voxel.BinaryVoxels;
import org.anchoranalysis.image.extent.Extent;
import org.anchoranalysis.image.extent.Resolution;
import org.anchoranalysis.image.object.ObjectMask;
import org.anchoranalysis.image.voxel.extracter.VoxelsExtracter;

public class VisitSchedulerConvexAboutRoot extends VisitScheduler {

    private Point3i root;

    @Override
    public Optional<Tuple3i> maxDistanceFromRootPoint(Resolution resolution) {
        return Optional.empty();
    }

    @Override
    public void beforeCreateObject(RandomNumberGenerator randomNumberGenerator, Resolution resolution)
            throws InitException {
        // NOTHING TO DO
    }

    @Override
    public void afterCreateObject(
            Point3i root, Resolution resolution, RandomNumberGenerator randomNumberGenerator)
            throws InitException {
        this.root = root;
    }

    private static Point3d relVector(Point3i root, Point3i point) {
        // Calculate angle from root to point
        Point3d rootSub = PointConverter.doubleFromInt(root);

        Point3d pointNew = PointConverter.doubleFromInt(point);
        pointNew.subtract(rootSub);
        return pointNew;
    }

    public static boolean isPointConvexTo(
            Point3i root, Point3i point, BinaryVoxels<ByteBuffer> bvb) {
        return isPointConvexTo(root, point, bvb, false);
    }

    public static boolean isPointConvexTo(
            Point3i root, Point3i destPoint, BinaryVoxels<ByteBuffer> voxels, boolean debug) {

        Point3d distance = relVector(root, destPoint);
        double mag =
                Math.pow(
                        Math.pow(distance.x(), 2)
                                + Math.pow(distance.y(), 2)
                                + Math.pow(distance.z(), 2),
                        0.5);

        if (mag > 0.0) {
            distance.scale(1 / mag);
        }

        // Now we keep checking that points are inside the region until we reach our final point
        Point3d point = PointConverter.doubleFromInt(root);
        VoxelsExtracter<ByteBuffer> extracter = voxels.extract();
        while (!pointEquals(point, destPoint)) {

            if (debug) {
                System.out.printf("%s ", point.toString()); // NOSONAR
            }

            if (!isPointOnObject(point, extracter, voxels.extent(), voxels.binaryValues())) {

                if (debug) {
                    System.out.printf("failed%n%n"); // NOSONAR
                }
                return false;
            }

            point.increment(distance);
        }

        return true;
    }

    @Override
    public boolean considerVisit(Point3i point, int distanceAlongContour, ObjectMask object) {
        return isPointConvexTo(root, point, object.binaryVoxels());
    }

    private static boolean pointEquals(Point3d point1, Point3i point2) {
        return point1.distanceSquared(point2) < 1.0;
    }

    private static boolean isPointOnObject(
            Point3d point, VoxelsExtracter<ByteBuffer> extracter, Extent extent, BinaryValues bv) {

        Point3i pointInt = PointConverter.intFromDoubleFloor(point);

        if (!extent.contains(pointInt)) {
            return false;
        }

        return extracter.voxel(pointInt) == bv.getOnInt();
    }
}
