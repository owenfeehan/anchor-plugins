/*-
 * #%L
 * anchor-plugin-image-task
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

package org.anchoranalysis.plugin.image.task.bean.grouped.raster;

import lombok.Getter;
import lombok.experimental.Accessors;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.image.channel.Channel;
import org.anchoranalysis.image.channel.factory.ChannelFactory;
import org.anchoranalysis.image.extent.ImageDimensions;
import org.anchoranalysis.image.voxel.datatype.VoxelDataType;
import org.anchoranalysis.image.voxel.datatype.UnsignedInt;

/**
 * A channel associated with a count. This is a useful structure for finding the mean of many
 * channels
 */
@Accessors(fluent = true)
class AggregateChnl {

    // We create only when we have the first channel, so dimensions can then be determined
    private Channel raster = null;
    @Getter private int count = 0;

    public synchronized void addChnl(Channel chnl) throws OperationFailedException {

        createRasterIfNecessary(chnl.dimensions());

        if (!chnl.dimensions().equals(raster.dimensions())) {
            throw new OperationFailedException(
                    String.format(
                            "Dimensions of added-chnl (%s) and aggregated-chnl must be equal (%s)",
                            chnl.dimensions(), raster.dimensions()));
        }

        VoxelsArithmetic.add(raster.voxels().asInt(), chnl.voxels(), chnl.getVoxelDataType());

        count++;
    }

    /**
     * Create a channel with the mean-value of all the aggregated channels
     *
     * @return the channel with newly created voxels
     * @throws OperationFailedException
     */
    public Channel createMeanChannel(VoxelDataType outputType) throws OperationFailedException {

        if (count == 0) {
            throw new OperationFailedException(
                    "No channels have been added, so cannot create mean");
        }

        Channel chnlOut = ChannelFactory.instance().create(raster.dimensions(), outputType);

        VoxelsArithmetic.divide(raster.voxels().asInt(), count, chnlOut.voxels(), outputType);

        return chnlOut;
    }

    private void createRasterIfNecessary(ImageDimensions dim) {
        if (raster == null) {
            this.raster = ChannelFactory.instance().create(dim, UnsignedInt.INSTANCE);
        }
    }
}
