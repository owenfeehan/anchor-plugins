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

package ch.ethz.biol.cell.imageprocessing.binaryimgchnl.provider;

import java.nio.ByteBuffer;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.image.binary.mask.Mask;
import org.anchoranalysis.image.binary.values.BinaryValuesByte;
import org.anchoranalysis.image.extent.Extent;

// Ors the receiveProvider onto the binaryImgChnlProvider
public class BinaryChnlProviderNot extends BinaryChnlProviderReceive {

    // ASSUMES REGIONS ARE IDENTICAL
    @Override
    protected Mask createFromChnlReceive(Mask maskCurrent, Mask maskReceiver) throws CreateException {

        BinaryValuesByte bvbCrnt = maskCurrent.binaryValues().createByte();
        BinaryValuesByte bvbReceiver = maskReceiver.binaryValues().createByte();

        Extent e = maskCurrent.dimensions().extent();

        byte crntOn = bvbCrnt.getOnByte();
        byte crntOff = bvbCrnt.getOffByte();
        byte receiveOff = bvbReceiver.getOffByte();

        // All the on voxels in the receive, are put onto crnt
        for (int z = 0; z < e.z(); z++) {

            ByteBuffer bufSrc = maskCurrent.voxels().slice(z).buffer();
            ByteBuffer bufReceive = maskReceiver.voxels().slice(z).buffer();

            int offset = 0;
            for (int y = 0; y < e.y(); y++) {
                for (int x = 0; x < e.x(); x++) {

                    byte byteSrc = bufSrc.get(offset);
                    if (byteSrc == crntOn) {

                        byte byteRec = bufReceive.get(offset);
                        if (byteRec == receiveOff) {
                            bufSrc.put(offset, crntOn);
                        } else {
                            bufSrc.put(offset, crntOff);
                        }
                    }

                    offset++;
                }
            }
        }

        return maskCurrent;
    }
}
