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
import org.anchoranalysis.image.binary.mask.Mask;
import org.anchoranalysis.image.binary.values.BinaryValuesByte;
import org.anchoranalysis.image.channel.Channel;
import org.anchoranalysis.image.convert.ByteConverter;
import org.anchoranalysis.image.extent.Extent;
import org.anchoranalysis.image.voxel.box.VoxelBox;
import lombok.Getter;
import lombok.Setter;

public class ChnlProviderSubtractMean extends ChnlProviderOneMask {

    // START BEAN PROPERTIES
    @BeanField @Getter @Setter private boolean subtractFromMaskOnly = true;
    // END BEAN PROPERTIES

    @Override
    protected Channel createFromMaskedChnl(Channel chnl, Mask mask) throws CreateException {

        double mean = calculateMean(chnl, mask);

        int meanInt = (int) Math.round(mean);

        if (subtractFromMaskOnly) {
            subtractMeanMask(chnl, mask, meanInt);
        } else {
            subtractMeanAll(chnl, meanInt);
        }

        return chnl;
    }

    private double calculateMean(Channel chnl, Mask mask) {

        VoxelBox<ByteBuffer> vbMask = mask.getChannel().getVoxelBox().asByte();
        VoxelBox<ByteBuffer> vbIntensity = chnl.getVoxelBox().asByte();

        Extent e = vbMask.extent();

        BinaryValuesByte bvb = mask.getBinaryValues().createByte();

        double sum = 0.0;
        double cnt = 0;

        for (int z = 0; z < e.getZ(); z++) {

            ByteBuffer bbMask = vbMask.getPixelsForPlane(z).buffer();
            ByteBuffer bbIntensity = vbIntensity.getPixelsForPlane(z).buffer();

            int offset = 0;
            for (int y = 0; y < e.getY(); y++) {
                for (int x = 0; x < e.getX(); x++) {

                    if (bbMask.get(offset) == bvb.getOnByte()) {
                        int intens = ByteConverter.unsignedByteToInt(bbIntensity.get(offset));
                        sum += intens;
                        cnt++;
                    }

                    offset++;
                }
            }
        }

        if (cnt == 0) {
            return 0;
        }

        return sum / cnt;
    }

    private void subtractMeanMask(Channel chnl, Mask mask, int mean) {

        VoxelBox<ByteBuffer> vbMask = mask.getChannel().getVoxelBox().asByte();
        VoxelBox<ByteBuffer> vbIntensity = chnl.getVoxelBox().asByte();

        Extent e = vbMask.extent();

        BinaryValuesByte bvb = mask.getBinaryValues().createByte();

        for (int z = 0; z < e.getZ(); z++) {

            ByteBuffer bbMask = vbMask.getPixelsForPlane(z).buffer();
            ByteBuffer bbIntensity = vbIntensity.getPixelsForPlane(z).buffer();

            int offset = 0;
            for (int y = 0; y < e.getY(); y++) {
                for (int x = 0; x < e.getX(); x++) {

                    if (bbMask.get(offset) == bvb.getOnByte()) {
                        int intens = ByteConverter.unsignedByteToInt(bbIntensity.get(offset));
                        int intensSub = (intens - mean);

                        if (intensSub < 0) {
                            intensSub = 0;
                        }

                        bbIntensity.put(offset, (byte) intensSub);
                    }

                    offset++;
                }
            }
        }
    }

    private void subtractMeanAll(Channel chnl, int mean) {

        VoxelBox<ByteBuffer> vbIntensity = chnl.getVoxelBox().asByte();

        Extent e = vbIntensity.extent();

        for (int z = 0; z < e.getZ(); z++) {

            ByteBuffer bbIntensity = vbIntensity.getPixelsForPlane(z).buffer();

            int offset = 0;
            for (int y = 0; y < e.getY(); y++) {
                for (int x = 0; x < e.getX(); x++) {

                    int intens = ByteConverter.unsignedByteToInt(bbIntensity.get(offset));
                    int intensSub = (intens - mean);

                    if (intensSub < 0) {
                        intensSub = 0;
                    }

                    bbIntensity.put(offset, (byte) intensSub);

                    offset++;
                }
            }
        }
    }
}
