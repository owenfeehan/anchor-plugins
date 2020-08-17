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
import org.anchoranalysis.image.convert.ByteConverter;
import org.anchoranalysis.image.voxel.Voxels;

public class ChnlProviderSubtract extends ChnlProviderTwoVoxelMapping {

    @Override
    protected void processVoxels(
            Voxels<ByteBuffer> voxelsOut,
            Voxels<ByteBuffer> voxelsIn1,
            Voxels<ByteBuffer> voxelsIn2) {

        for (int z = 0; z < voxelsOut.extent().z(); z++) {

            ByteBuffer in1 = voxelsIn1.sliceBuffer(z);
            ByteBuffer in2 = voxelsIn2.sliceBuffer(z);
            ByteBuffer out = voxelsOut.sliceBuffer(z);

            while (in1.hasRemaining()) {

                byte b1 = in1.get();
                byte b2 = in2.get();

                int diff =
                        ByteConverter.unsignedByteToInt(b1) - ByteConverter.unsignedByteToInt(b2);
                out.put((byte) diff);
            }

            assert (!in2.hasRemaining());
            assert (!out.hasRemaining());
        }
    }
}
