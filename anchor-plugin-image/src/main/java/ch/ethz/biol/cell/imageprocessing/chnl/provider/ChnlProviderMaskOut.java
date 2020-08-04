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
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.image.binary.mask.Mask;
import org.anchoranalysis.image.channel.Channel;
import org.anchoranalysis.image.channel.factory.ChannelFactory;
import org.anchoranalysis.image.extent.BoundingBox;
import org.anchoranalysis.image.voxel.box.VoxelBox;

/**
 * Set pixels NOT IN the mask to 0, but keep pixels IN the mask identical.
 *
 * <p>It's an immutable operation and a new channel is always produced.
 *
 * @author Owen Feehan
 */
public class ChnlProviderMaskOut extends ChnlProviderOneMask {

    @Override
    protected Channel createFromMaskedChnl(Channel chnl, Mask mask) throws CreateException {

        VoxelBox<ByteBuffer> vbMask = mask.getChannel().voxels().asByte();

        Channel chnlOut =
                ChannelFactory.instance()
                        .createEmptyInitialised(chnl.getDimensions(), chnl.getVoxelDataType());

        BoundingBox bbox = new BoundingBox(chnlOut.getDimensions().getExtent());
        chnl.voxels()
                .copyPixelsToCheckMask(
                        bbox,
                        chnlOut.voxels(),
                        bbox,
                        vbMask,
                        mask.getBinaryValues().createByte());

        return chnlOut;
    }
}
