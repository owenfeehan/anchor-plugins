/*-
 * #%L
 * anchor-plugin-image
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
package org.anchoranalysis.plugin.image.bean.thumbnail.object;

import java.util.Optional;
import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.annotation.OptionalBean;
import org.anchoranalysis.core.color.ColorIndex;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.core.functional.StreamableCollection;
import org.anchoranalysis.image.bean.interpolator.InterpolatorBean;
import org.anchoranalysis.image.bean.interpolator.InterpolatorBeanLanczos;
import org.anchoranalysis.image.bean.size.SizeXY;
import org.anchoranalysis.image.extent.BoundingBox;
import org.anchoranalysis.image.extent.Extent;
import org.anchoranalysis.image.interpolator.Interpolator;
import org.anchoranalysis.image.io.generator.raster.boundingbox.DrawObjectOnStackGenerator;
import org.anchoranalysis.image.io.generator.raster.boundingbox.ScaleableBackground;
import org.anchoranalysis.image.object.ObjectCollection;
import org.anchoranalysis.image.object.ObjectsWithBoundingBox;
import org.anchoranalysis.image.stack.DisplayStack;
import org.anchoranalysis.image.stack.Stack;
import org.anchoranalysis.io.bean.color.RGBColorBean;
import org.anchoranalysis.io.output.error.OutputWriteFailedException;

/**
 * Create a thumbnail by drawing an outline of an object at a particular-scale, and placing it
 * centered in a window of a certain size.
 *
 * <p>Preserves the relative-size between objects (i.e. they are all reduced by the same
 * scale-factor) and all aspect-ratios.
 *
 * <p>If it's a z-stack, a maximum intensity projection is first applied.
 *
 * <p>All thumbnails are created with identical size. An error will occur if the background is ever
 * smaller than the thumbnail size.
 *
 * <p>If no specific background-channel is set with {@link backgroundChannelIndex} then the
 * following scheme applies:
 *
 * <ul>
 *   <li>If {@code backgroundSource} has exactly zero channels, an empty zero-valued monochrome
 *       background is used (unsigned 8 bit).
 *   <li>If {@code backgroundSource} has exactly one channel, it's used as a monochrome background
 *   <li>If {@code backgroundSource} has exactly three channels, it's used as a RGB background
 *   <li>If {@code backgroundSource} has any other number of channels, the first channel is used as
 *       a background.
 * </ul>
 *
 * @author Owen Feehan
 */
public class OutlinePreserveRelativeSize extends ThumbnailFromObjects {

    // START BEAN PROPERTIES
    /** Size of all created thumbnails */
    @BeanField @Getter @Setter private SizeXY size = new SizeXY(200, 200);

    /**
     * Uses only this channel (identified by an index in the stack) as the background, -1 disables.
     */
    @BeanField @Getter @Setter private int backgroundChannelIndex = -1;

    /** Interpolator used when scaling the background */
    @BeanField @Getter @Setter
    private InterpolatorBean interpolator = new InterpolatorBeanLanczos();

    /**
     * The width of the outline. By default, it's 3 as it's nice to have a strongly easily-visible
     * emphasis on where the object is in a thumbnail.
     */
    @BeanField @Getter @Setter private int outlineWidth = 3;

    /**
     * Optionally outline the other (unselected for the thumbnail) objects in this particular color.
     * If not set, these objects aren't outlined at all..
     */
    @BeanField @OptionalBean @Getter @Setter
    private RGBColorBean colorUnselectedObjects = new RGBColorBean(0, 0, 255);
    // END BEAN PROPERTIES

    private DrawObjectOnStackGenerator generator;
    private FlattenAndScaler scaler;
    private Extent sceneExtentScaled;

    @Override
    public void start(
            ObjectCollection objects,
            StreamableCollection<BoundingBox> boundingBoxes,
            Optional<Stack> backgroundSource)
            throws OperationFailedException {

        if (objects.isEmpty()) {
            // Nothing to do, no thumbnails will ever be generated
            return;
        }

        Interpolator interpolatorBackground = interpolator.create();

        // Determine what to scale the objects and any background by
        scaler =
                new FlattenAndScaler(
                        boundingBoxes, objects, interpolatorBackground, size.asExtent());

        setupGenerator(
                objects,
                determineBackgroundMaybeOutlined(backgroundSource, interpolatorBackground));
    }

    @Override
    public DisplayStack thumbnailFor(ObjectCollection objects) throws CreateException {

        // For now only work with the first object in the collection
        try {
            ObjectsWithBoundingBox objectsScaled =
                    new ObjectsWithBoundingBox(scaler.scaleObjects(objects));

            assert (!objectsScaled
                    .boundingBox()
                    .extent()
                    .anyDimensionIsLargerThan(size.asExtent()));

            // Find a bounding-box of target size in which objectScaled is centered
            BoundingBox centeredBox =
                    CenterBoundingBoxHelper.deriveCenteredBoxWithSize(
                            objectsScaled.boundingBox(), size.asExtent(), sceneExtentScaled);

            assert centeredBox.extent().equals(size.asExtent());
            assert sceneExtentScaled.contains(centeredBox);

            generator.setIterableElement(determineObjectsForGenerator(objectsScaled, centeredBox));

            return DisplayStack.create(generator.generate());

        } catch (OutputWriteFailedException | OperationFailedException e) {
            throw new CreateException(e);
        }
    }

    @Override
    public void end() {
        // Garbage collect the scaler as it contains a cache of scaled-objects
        scaler = null;
    }

    private ObjectsWithBoundingBox determineObjectsForGenerator(
            ObjectsWithBoundingBox objectsScaled, BoundingBox centeredBox)
            throws OperationFailedException {
        ObjectsWithBoundingBox objectsMapped = objectsScaled.mapBoundingBoxToBigger(centeredBox);

        // Add any other objects which intersect with the scaled-bounding box, excluding
        //  the object themselves
        if (colorUnselectedObjects != null) {
            return objectsMapped.addObjectsNoBoundingBoxChange(
                    scaler.objectsThatIntersectWith(
                            objectsMapped.boundingBox(), objectsScaled.objects()));
        } else {
            return objectsMapped;
        }
    }

    /**
     * Sets up the generator and the related {@code sceneExtentScaled} variable
     *
     * @param objectsUnscaled unscaled objects
     * @param backgroundUnscaled unscaled background if it exists
     */
    private void setupGenerator(
            ObjectCollection objectsUnscaled, Optional<ScaleableBackground> backgroundScaled) {

        sceneExtentScaled = scaler.extentFromStackOrObjects(backgroundScaled, objectsUnscaled);

        // Create a generator that draws objects on the background
        generator =
                DrawObjectOnStackGenerator.createFromStack(
                        backgroundScaled, outlineWidth, createColorIndex(false));
    }

    private Optional<ScaleableBackground> determineBackgroundMaybeOutlined(
            Optional<Stack> backgroundSource, Interpolator interpolator) {
        BackgroundSelector backgroundHelper =
                new BackgroundSelector(
                        backgroundChannelIndex, scaler.getScaleFactor(), interpolator);
        return backgroundHelper.determineBackground(backgroundSource);
    }

    /**
     * Creates a suitable color index for distinguishing between the different types of objects that
     * appear
     *
     * @param pairs whether pairs are being used or not
     * @return the color index
     */
    private ColorIndex createColorIndex(boolean pairs) {
        return new ThumbnailColorIndex(pairs, colorUnselectedObjects.toAWTColor());
    }
}
