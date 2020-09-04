/*-
 * #%L
 * anchor-image
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

package org.anchoranalysis.plugin.imagej.bean.interpolator;

import ij.process.ImageProcessor;
import org.anchoranalysis.image.convert.UnsignedByteBuffer;
import java.nio.ShortBuffer;
import org.anchoranalysis.image.extent.Extent;
import org.anchoranalysis.image.interpolator.Interpolator;
import org.anchoranalysis.image.voxel.buffer.VoxelBuffer;
import org.anchoranalysis.io.imagej.convert.ConvertToVoxelBuffer;
import org.anchoranalysis.io.imagej.convert.ConvertToImageProcessor;

class InterpolatorImageJ implements Interpolator {

    @Override
    public VoxelBuffer<UnsignedByteBuffer> interpolateByte(
            VoxelBuffer<UnsignedByteBuffer> voxelsSource,
            VoxelBuffer<UnsignedByteBuffer> voxelsDestination,
            Extent extentSource,
            Extent extentDestination) {

        ImageProcessor source = ConvertToImageProcessor.fromByte(voxelsSource, extentSource);
        ImageProcessor out = source.resize(extentDestination.x(), extentDestination.y(), true);
        return ConvertToVoxelBuffer.asByte(out);
    }

    @Override
    public VoxelBuffer<ShortBuffer> interpolateShort(
            VoxelBuffer<ShortBuffer> voxelsSource,
            VoxelBuffer<ShortBuffer> voxelsDestination,
            Extent extentSource,
            Extent extentDestination) {

        ImageProcessor source = ConvertToImageProcessor.fromShort(voxelsSource, extentSource);
        ImageProcessor out = source.resize(extentDestination.x(), extentDestination.y(), true);
        return ConvertToVoxelBuffer.asShort(out);
    }

    @Override
    public boolean isNewValuesPossible() {
        return true;
    }
}
