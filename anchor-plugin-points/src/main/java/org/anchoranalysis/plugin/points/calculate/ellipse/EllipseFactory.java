/*-
 * #%L
 * anchor-plugin-points
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

package org.anchoranalysis.plugin.points.calculate.ellipse;

import java.util.List;
import java.util.Set;
import lombok.AllArgsConstructor;
import org.anchoranalysis.core.exception.CreateException;
import org.anchoranalysis.core.functional.FunctionalList;
import org.anchoranalysis.image.core.dimensions.Dimensions;
import org.anchoranalysis.image.core.points.PointsFromObject;
import org.anchoranalysis.image.voxel.object.ObjectMask;
import org.anchoranalysis.mpp.bean.points.fitter.InsufficientPointsException;
import org.anchoranalysis.mpp.bean.points.fitter.PointsFitterException;
import org.anchoranalysis.mpp.mark.conic.Ellipse;
import org.anchoranalysis.plugin.points.bean.fitter.ConicFitterBase;
import org.anchoranalysis.spatial.point.Point3f;
import org.anchoranalysis.spatial.point.Point3i;
import org.anchoranalysis.spatial.point.PointConverter;

@AllArgsConstructor
class EllipseFactory {

    private static final int MIN_NUMBER_POINTS = 10;

    private final ConicFitterBase pointsFitter;

    public Ellipse create(ObjectMask object, Dimensions dimensions, double shell)
            throws CreateException, InsufficientPointsException {

        pointsFitter.setShell(shell);

        Set<Point3i> points = PointsFromObject.setFromContour(object);

        if (points.size() < MIN_NUMBER_POINTS) {
            throw new InsufficientPointsException(
                    String.format(
                            "Only %d points. There must be at least %d points.",
                            points.size(), MIN_NUMBER_POINTS));
        }

        return createEllipse(points, dimensions);
    }

    private Ellipse createEllipse(Set<Point3i> points, Dimensions dim)
            throws InsufficientPointsException, CreateException {

        Ellipse mark = new Ellipse();

        List<Point3f> pointsAsFloat =
                FunctionalList.mapToList(points, PointConverter::floatFromIntDropZ);

        try {
            pointsFitter.fit(pointsAsFloat, mark, dim);
        } catch (PointsFitterException e) {
            throw new CreateException(e);
        }

        return mark;
    }
}
