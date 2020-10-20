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

package org.anchoranalysis.plugin.points.bean.fitter;

import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.annotation.Positive;
import org.anchoranalysis.core.exception.CreateException;
import org.anchoranalysis.core.exception.OperationFailedException;
import org.anchoranalysis.image.bean.provider.DimensionsProvider;
import org.anchoranalysis.image.bean.provider.ObjectCollectionProvider;
import org.anchoranalysis.image.core.dimensions.Dimensions;
import org.anchoranalysis.image.voxel.object.ObjectCollection;
import org.anchoranalysis.mpp.bean.points.PointsBean;
import org.anchoranalysis.mpp.bean.points.fitter.InsufficientPointsException;
import org.anchoranalysis.mpp.bean.points.fitter.PointsFitter;
import org.anchoranalysis.mpp.bean.points.fitter.PointsFitterException;
import org.anchoranalysis.mpp.mark.Mark;
import org.anchoranalysis.spatial.point.Point3f;

public class PointsFitterToMark extends PointsBean<PointsFitterToMark> {

    // START BEAN PROPERTIES
    @BeanField @Getter @Setter private PointsFitter pointsFitter;

    @BeanField @Getter @Setter private DimensionsProvider dimensions;

    /** If an object has fewer points than before being fitted, we ignore */
    @BeanField @Positive @Getter @Setter private int minNumPoints = 1;

    @BeanField @Getter @Setter private ObjectCollectionProvider objects;
    // END BEAN PROPERTIES

    public void fitPointsToMark(List<Point3f> pointsForFitter, Mark mark, Dimensions dim)
            throws OperationFailedException {
        try {
            pointsFitter.fit(pointsForFitter, mark, dim);
        } catch (PointsFitterException | InsufficientPointsException e) {
            throw new OperationFailedException(e);
        }
    }

    public ObjectCollection createObjects() throws CreateException {
        return objects.create();
    }

    public Dimensions createDim() throws CreateException {
        return dimensions.create();
    }
}
