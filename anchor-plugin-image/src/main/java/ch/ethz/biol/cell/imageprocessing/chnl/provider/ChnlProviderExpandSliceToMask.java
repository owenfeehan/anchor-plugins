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

package ch.ethz.biol.cell.imageprocessing.chnl.provider;

import java.nio.ByteBuffer;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.image.bean.provider.ChnlProvider;
import org.anchoranalysis.image.channel.Channel;
import org.anchoranalysis.image.channel.factory.ChannelFactory;
import org.anchoranalysis.image.extent.ImageDimensions;
import org.anchoranalysis.image.voxel.box.VoxelBox;
import org.anchoranalysis.image.voxel.buffer.VoxelBufferByte;
import org.anchoranalysis.image.voxel.datatype.IncorrectVoxelDataTypeException;
import org.anchoranalysis.image.voxel.datatype.VoxelDataTypeUnsignedByte;
import lombok.Getter;
import lombok.Setter;

/**
 * Takes a 2-dimensional mask and converts into a 3-dimensional mask along the z-stack but discards
 * empty slices in a binary on the top and bottom
 */
public class ChnlProviderExpandSliceToMask extends ChnlProvider {

    // START BEAN PROPERTIES
    @BeanField @Getter @Setter private ChnlProvider chnlTargetDimensions;

    @BeanField @Getter @Setter private ChnlProvider chnlSlice;
    // END BEAN PROPERTIES

    @Override
    public Channel create() throws CreateException {

        ImageDimensions sdTarget = chnlTargetDimensions.create().getDimensions();

        Channel slice = chnlSlice.create();

        checkDimensions(slice.getDimensions(), sdTarget);

        try {
            return createExpandedChnl(sdTarget, slice.getVoxelBox().asByte());
        } catch (IncorrectVoxelDataTypeException e) {
            throw new CreateException("chnlSlice must have unsigned 8 bit data");
        }
    }

    private static void checkDimensions(ImageDimensions dimSrc, ImageDimensions dimTarget)
            throws CreateException {
        if (dimSrc.getX() != dimTarget.getX()) {
            throw new CreateException("x dimension is not equal");
        }
        if (dimSrc.getY() != dimTarget.getY()) {
            throw new CreateException("y dimension is not equal");
        }
    }

    private Channel createExpandedChnl(ImageDimensions sdTarget, VoxelBox<ByteBuffer> vbSlice) {

        Channel chnl =
                ChannelFactory.instance()
                        .createEmptyUninitialised(sdTarget, VoxelDataTypeUnsignedByte.INSTANCE);

        VoxelBox<ByteBuffer> vbOut = chnl.getVoxelBox().asByte();

        for (int z = 0; z < chnl.getDimensions().getZ(); z++) {
            ByteBuffer bb = vbSlice.duplicate().getPixelsForPlane(0).buffer();
            vbOut.setPixelsForPlane(z, VoxelBufferByte.wrap(bb));
        }

        return chnl;
    }
}
