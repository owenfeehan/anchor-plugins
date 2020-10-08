/*-
 * #%L
 * anchor-plugin-io
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
package org.anchoranalysis.plugin.io.bean.rasterwriter.bioformats;

import java.io.IOException;
import loci.formats.FormatException;
import loci.formats.IFormatWriter;
import org.anchoranalysis.image.channel.Channel;
import org.anchoranalysis.image.convert.UnsignedByteBuffer;
import org.anchoranalysis.image.io.RasterIOException;
import org.anchoranalysis.image.stack.Stack;

class RGBWriterShort extends RGBWriter {

    public RGBWriterShort(IFormatWriter writer, Stack stack) {
        super(writer, stack);
    }

    @Override
    protected void mergeSliceAsRGB(int z, int capacity) throws RasterIOException {
        UnsignedByteBuffer merged = UnsignedByteBuffer.allocate(capacity * 3 * 2);
        putSliceShort(merged, channelRed, z);
        putSliceShort(merged, channelGreen, z);
        putSliceShort(merged, channelBlue, z);
        try {
            writer.saveBytes(z, merged.array());
        } catch (FormatException | IOException e) {
            throw new RasterIOException(e);
        }
    }

    private static void putSliceShort(UnsignedByteBuffer merged, Channel channel, int z) {
        merged.getDelegate()
                .asShortBuffer()
                .put(channel.voxels().asShort().sliceBuffer(z).getDelegate());
    }
}
