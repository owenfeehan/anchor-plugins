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
import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.BeanInstanceMap;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.annotation.Positive;
import org.anchoranalysis.bean.error.BeanMisconfiguredException;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.image.bean.provider.ChnlProviderOne;
import org.anchoranalysis.image.channel.Channel;
import org.anchoranalysis.image.channel.factory.ChannelFactoryByte;
import org.anchoranalysis.image.extent.Extent;
import org.anchoranalysis.image.extent.ImageDimensions;
import org.anchoranalysis.image.voxel.Voxels;

public class ChnlProviderExtractSliceRange extends ChnlProviderOne {

    // START BEANS
    @BeanField @Positive @Getter @Setter private int sliceStart;

    @BeanField @Positive @Getter @Setter private int sliceEnd;
    // END BEANS

    @Override
    public Channel createFromChnl(Channel chnl) throws CreateException {

        ChannelFactoryByte factory = new ChannelFactoryByte();

        Voxels<ByteBuffer> voxels = chnl.voxels().asByte();

        Extent e = chnl.dimensions().extent().duplicateChangeZ(sliceEnd - sliceStart + 1);

        Channel chnlOut =
                factory.createEmptyInitialised(
                        new ImageDimensions(e, chnl.dimensions().resolution()));
        Voxels<ByteBuffer> voxelsOut = chnlOut.voxels().asByte();

        int volumeXY = voxels.extent().volumeXY();
        for (int z = sliceStart; z <= sliceEnd; z++) {

            ByteBuffer bbIn = voxels.slice(z).buffer();
            ByteBuffer bbOut = voxelsOut.slice(z - sliceStart).buffer();

            for (int i = 0; i < volumeXY; i++) {
                bbOut.put(i, bbIn.get(i));
            }
        }

        return chnlOut;
    }

    @Override
    public void checkMisconfigured(BeanInstanceMap defaultInstances)
            throws BeanMisconfiguredException {
        super.checkMisconfigured(defaultInstances);
        if (sliceEnd < sliceStart) {
            throw new BeanMisconfiguredException("SliceStart must be less than SliceEnd");
        }
    }
}
