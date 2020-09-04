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

package org.anchoranalysis.plugin.image.bean.object.provider.segment;

import org.anchoranalysis.image.convert.UnsignedByteBuffer;
import java.util.Optional;
import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.image.bean.nonbean.error.SegmentationFailedException;
import org.anchoranalysis.image.bean.nonbean.parameters.BinarySegmentationParameters;
import org.anchoranalysis.image.bean.segment.binary.BinarySegmentation;
import org.anchoranalysis.image.binary.voxel.BinaryVoxels;
import org.anchoranalysis.image.channel.Channel;
import org.anchoranalysis.image.object.ObjectCollection;
import org.anchoranalysis.image.object.ObjectMask;
import org.anchoranalysis.image.voxel.Voxels;
import org.anchoranalysis.image.voxel.VoxelsWrapper;
import org.anchoranalysis.plugin.image.bean.object.provider.UnaryWithChannelBase;

/**
 * Performs a binary-segmentation using the upstream objects as masks.
 *
 * <p>Note that if there is more than one upstream object, multiple segmentations occur (one for
 * each mask) and are then combined.
 *
 * @author Owen Feehan
 */
public class BinarySegmentByObject extends UnaryWithChannelBase {

    // START BEAN PROPERTIES
    @BeanField @Getter @Setter private BinarySegmentation binarySgmn;
    // END BEAN PROPERTIES

    @Override
    protected ObjectCollection createFromObjects(
            ObjectCollection objectsSource, Channel channelSource) throws CreateException {
        try {
            return objectsSource.stream().map(object -> sgmnObject(object, channelSource));
        } catch (SegmentationFailedException e) {
            throw new CreateException(e);
        }
    }

    private ObjectMask sgmnObject(ObjectMask object, Channel channelSource)
            throws SegmentationFailedException {
        Voxels<?> voxels = channelSource.extract().region(object.boundingBox(), true);

        BinaryVoxels<UnsignedByteBuffer> bvb =
                binarySgmn.segment(
                        new VoxelsWrapper(voxels),
                        new BinarySegmentationParameters(),
                        Optional.of(new ObjectMask(object.voxels())));

        return new ObjectMask(object.boundingBox(), bvb);
    }
}
