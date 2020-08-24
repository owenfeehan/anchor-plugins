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

package org.anchoranalysis.plugin.mpp.bean.outline;

import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.provider.Provider;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.error.InitException;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.core.geometry.Point3i;
import org.anchoranalysis.core.geometry.Tuple3i;
import org.anchoranalysis.core.random.RandomNumberGenerator;
import org.anchoranalysis.image.binary.mask.Mask;
import org.anchoranalysis.image.extent.BoundingBox;
import org.anchoranalysis.image.extent.Extent;
import org.anchoranalysis.image.extent.Dimensions;
import org.anchoranalysis.image.extent.Resolution;
import org.anchoranalysis.image.object.ObjectMask;
import org.anchoranalysis.image.outline.traverser.OutlineTraverser;
import org.anchoranalysis.plugin.mpp.bean.outline.visitscheduler.VisitScheduler;

// Traverses a pixel location
public class TraverseOutlineOnImage extends OutlinePixelsRetriever {

    // START BEAN PROPERTIES
    @BeanField @Getter @Setter private VisitScheduler visitScheduler;

    @BeanField @Getter @Setter private boolean bigNeighborhood = true;

    @BeanField @Getter @Setter private Provider<Mask> maskOutline;

    @BeanField @Getter @Setter private Provider<Mask> maskFilled;

    @BeanField @Getter @Setter private boolean useZ = true;
    // END BEAN PROPERTIES

    private ObjectMask objectFilled; // Not-changed as traversal occurs
    private ObjectMask objectOutline; // Changed as traversal occurs (visited pixels are removed)

    @Override
    public void traverse(
            Point3i root, List<Point3i> listOut, RandomNumberGenerator randomNumberGenerator)
            throws TraverseOutlineException {

        Mask outline = createOutline();
        Mask filled = createFilled();

        checkDimensions(outline.dimensions(), filled.dimensions());

        useZ = useZ && (outline.dimensions().z() > 1);

        callBefore(outline.dimensions().resolution(), randomNumberGenerator);

        objectOutline = createObjectForPoint(root, outline);

        objectFilled = objectForFilled(root, filled);
        callAfter(root, outline.dimensions().resolution(), randomNumberGenerator);
        traverseOutline(root, listOut);
    }

    private Mask createOutline() throws TraverseOutlineException {
        try {
            return maskOutline.create();
        } catch (CreateException e) {
            throw new TraverseOutlineException("Unable to create binaryImgChannelProviderOutline", e);
        }
    }

    private Mask createFilled() throws TraverseOutlineException {
        try {
            return maskFilled.create();
        } catch (CreateException e) {
            throw new TraverseOutlineException("Unable to create binaryImgChannelProviderFilled", e);
        }
    }

    private void checkDimensions(Dimensions dimOutline, Dimensions dimFilled)
            throws TraverseOutlineException {
        if (!dimOutline.equals(dimFilled)) {
            throw new TraverseOutlineException(
                    String.format("Dimensions %s and %s are not equal", dimOutline, dimFilled));
        }
    }

    private void callBefore(Resolution res, RandomNumberGenerator randomNumberGenerator)
            throws TraverseOutlineException {
        try {
            visitScheduler.beforeCreateObject(randomNumberGenerator, res);
        } catch (InitException e1) {
            throw new TraverseOutlineException(
                    "Failure to call beforeCreateObject on visitScheduler", e1);
        }
    }

    private ObjectMask objectForFilled(Point3i root, Mask mask) throws TraverseOutlineException {
        // Important, so we can use the contains function later
        return createObjectForPoint(root, mask).shiftToOrigin();
    }

    private void callAfter(
            Point3i root, Resolution res, RandomNumberGenerator randomNumberGenerator)
            throws TraverseOutlineException {
        Point3i rootRelToMask =
                BoundingBox.relativePositionTo(root, objectOutline.boundingBox().cornerMin());
        try {
            visitScheduler.afterCreateObject(rootRelToMask, res, randomNumberGenerator);
        } catch (InitException e) {
            throw new TraverseOutlineException(
                    "Cannot call afterCreateObject on visitScheduler", e);
        }
    }

    private void traverseOutline(Point3i root, List<Point3i> listOut)
            throws TraverseOutlineException {
        try {
            new OutlineTraverser(
                            objectOutline,
                            (point, distance) ->
                                    visitScheduler.considerVisit(point, distance, objectFilled),
                            useZ,
                            bigNeighborhood)
                    .applyGlobal(root, listOut);
        } catch (OperationFailedException e) {
            throw new TraverseOutlineException("Cannot traverse outline", e);
        }
    }

    private ObjectMask createObjectForPoint(Point3i root, Mask mask)
            throws TraverseOutlineException {

        try {
            Tuple3i maxDistance =
                    visitScheduler
                            .maxDistanceFromRootPoint(mask.dimensions().resolution())
                            .orElseThrow(
                                    () ->
                                            new CreateException(
                                                    "An undefined max-distance is not supported"));

            // We make sure the box is within our scene boundaries
            BoundingBox box = createBoxAroundPoint(root, maxDistance, mask.extent());

            // This is our final intersection box, that we use for traversing and memorizing pixels
            //  that we have already visited
            assert (!box.extent().isEmpty());

            return mask.region(box, false);

        } catch (OperationFailedException | CreateException e) {
            throw new TraverseOutlineException(
                    "Unable to create an object-mask for the outline", e);
        }
    }

    private static BoundingBox createBoxAroundPoint(Point3i point, Tuple3i width, Extent clipTo) {
        // We create a raster around the point, maxDistance*2 in both directions, so long as it
        // doesn't escape the region
        BoundingBox box =
                new BoundingBox(
                        Point3i.immutableSubtract(point, width),
                        Point3i.immutableAdd(point, width));
        return box.clipTo(clipTo);
    }
}
