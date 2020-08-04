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

package org.anchoranalysis.plugin.image.bean.object.segment;

import java.nio.ByteBuffer;
import java.util.Optional;
import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.geometry.ReadableTuple3i;
import org.anchoranalysis.image.bean.nonbean.error.SegmentationFailedException;
import org.anchoranalysis.image.bean.nonbean.parameters.BinarySegmentationParameters;
import org.anchoranalysis.image.bean.segment.binary.BinarySegmentation;
import org.anchoranalysis.image.bean.segment.object.SegmentChannelIntoObjects;
import org.anchoranalysis.image.binary.mask.Mask;
import org.anchoranalysis.image.binary.voxel.BinaryVoxels;
import org.anchoranalysis.image.channel.Channel;
import org.anchoranalysis.image.extent.ImageResolution;
import org.anchoranalysis.image.object.ObjectCollection;
import org.anchoranalysis.image.object.ObjectMask;
import org.anchoranalysis.image.object.factory.CreateFromConnectedComponentsFactory;
import org.anchoranalysis.image.seed.SeedCollection;

/**
 * Performs a binary-segmentation of the channel and converts its connected-components into objects
 *
 * @author Owen Feehan
 */
public class ConnectedComponentsFromBinarySegmentation extends SegmentChannelIntoObjects {

    // START BEAN PROPERTIES
    @BeanField @Getter @Setter private BinarySegmentation segment;

    @BeanField @Getter @Setter private int minNumberVoxels = 1;
    // END BEAN PROPERTIES

    @Override
    public ObjectCollection segment(
            Channel channel, Optional<ObjectMask> objectMask, Optional<SeedCollection> seeds)
            throws SegmentationFailedException {

        BinarySegmentationParameters params =
                new BinarySegmentationParameters(channel.getDimensions().getResolution());

        BinaryVoxels<ByteBuffer> bvb = segment.segment(channel.voxels(), params, objectMask);
        return createFromBinaryVoxels(
                bvb,
                channel.getDimensions().getResolution(),
                objectMask.map(object -> object.getBoundingBox().cornerMin()));
    }

    private ObjectCollection createFromBinaryVoxels(
            BinaryVoxels<ByteBuffer> bvb,
            ImageResolution resolution,
            Optional<ReadableTuple3i> maskShiftBy)
            throws SegmentationFailedException {
        Mask bic = new Mask(bvb, resolution);

        CreateFromConnectedComponentsFactory creator =
                new CreateFromConnectedComponentsFactory(minNumberVoxels);
        try {
            return maybeShiftObjects(creator.createConnectedComponents(bic), maskShiftBy);
        } catch (CreateException e) {
            throw new SegmentationFailedException(e);
        }
    }

    private static ObjectCollection maybeShiftObjects(
            ObjectCollection objects, Optional<ReadableTuple3i> shiftByQuantity) {
        return shiftByQuantity.map(objects::shiftBy).orElse(objects);
    }
}
