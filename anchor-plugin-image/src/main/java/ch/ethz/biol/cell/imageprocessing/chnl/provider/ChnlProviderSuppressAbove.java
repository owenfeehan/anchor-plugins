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
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.image.binary.mask.Mask;
import org.anchoranalysis.image.channel.Channel;
import org.anchoranalysis.image.convert.ByteConverter;
import org.anchoranalysis.image.extent.Extent;
import org.anchoranalysis.image.histogram.Histogram;
import org.anchoranalysis.image.histogram.HistogramFactory;
import org.anchoranalysis.image.voxel.Voxels;

// TODO consider using a generic histogram-feature and a FeatureCalculation to cache the histogram
// creation
// TODO consider using some form of histogram thresholder
public class ChnlProviderSuppressAbove extends ChnlProviderOneMask {

    // START BEAN PROPERTIES
    @BeanField @Getter @Setter private double quantile = 0.5;
    // END BEAN PROPERTIES

    @Override
    protected Channel createFromMaskedChannel(Channel channel, Mask mask) throws CreateException {

        Histogram hist = HistogramFactory.create(channel, mask);

        try {
            double intensityThrshldDbl = hist.quantile(quantile);

            replacePixelsAbove((int) Math.ceil(intensityThrshldDbl), channel.voxels().asByte());
        } catch (OperationFailedException e) {
            throw new CreateException("An error occurred computing a quantile", e);
        }

        return channel;
    }

    /** Replaces any pixels with value > threshold, with the threshold value */
    private static void replacePixelsAbove(int threshold, Voxels<ByteBuffer> voxels) {
        byte meanIntensityByte = (byte) threshold;

        Extent e = voxels.extent();

        for (int z = 0; z < e.z(); z++) {

            ByteBuffer bb = voxels.sliceBuffer(z);

            int offset = 0;
            for (int y = 0; y < e.y(); y++) {
                for (int x = 0; x < e.x(); x++) {

                    int val = ByteConverter.unsignedByteToInt(bb.get(offset));
                    if (val > threshold) {
                        bb.put(offset, meanIntensityByte);
                    }

                    offset++;
                }
            }
        }
    }
}
