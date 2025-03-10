/*-
 * #%L
 * anchor-plugin-image-feature
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

package org.anchoranalysis.plugin.image.feature.bean.object.single.intensity.gradient;

import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import org.anchoranalysis.core.exception.InitializeException;
import org.anchoranalysis.feature.calculate.FeatureCalculationException;
import org.anchoranalysis.feature.calculate.part.CalculationPart;
import org.anchoranalysis.feature.energy.EnergyStackWithoutParameters;
import org.anchoranalysis.image.core.channel.Channel;
import org.anchoranalysis.image.feature.input.FeatureInputSingleObject;
import org.anchoranalysis.image.voxel.Voxels;
import org.anchoranalysis.image.voxel.binary.BinaryVoxels;
import org.anchoranalysis.image.voxel.binary.values.BinaryValuesByte;
import org.anchoranalysis.image.voxel.buffer.VoxelBuffer;
import org.anchoranalysis.image.voxel.buffer.primitive.UnsignedByteBuffer;
import org.anchoranalysis.image.voxel.object.ObjectMask;
import org.anchoranalysis.spatial.box.BoundingBox;
import org.anchoranalysis.spatial.box.Extent;
import org.anchoranalysis.spatial.point.Point3d;

/**
 * When the image-gradient is supplied as multiple channels in an energy stack, this converts it
 * into a list of points
 *
 * <p>A constant is subtracted from the (all positive) image-channels, to make positive or negative
 * values
 *
 * @author Owen Feehan
 */
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
class CalculateGradientFromChannels
        extends CalculationPart<List<Point3d>, FeatureInputSingleObject> {

    private int energyIndexX;

    private int energyIndexY;

    // If -1, then no z-gradient is considered, and all z-values are 0
    private int energyIndexZ;

    private int subtractConstant = 0;

    @Override
    protected List<Point3d> execute(FeatureInputSingleObject input)
            throws FeatureCalculationException {

        if (energyIndexX == -1 || energyIndexY == -1) {
            throw new FeatureCalculationException(
                    new InitializeException("energyIndexX and energyIndexY must both be nonZero"));
        }

        // create a list of points
        List<Point3d> out = new ArrayList<>();

        EnergyStackWithoutParameters energyStack =
                input.getEnergyStackRequired().withoutParameters();

        putGradientValue(input.getObject(), out, 0, energyStack.getChannel(energyIndexX));
        putGradientValue(input.getObject(), out, 1, energyStack.getChannel(energyIndexY));

        if (energyIndexZ != -1) {
            putGradientValue(input.getObject(), out, 2, energyStack.getChannel(energyIndexZ));
        }

        return out;
    }

    // Always iterates over the list in the same-order
    private void putGradientValue(
            ObjectMask object, List<Point3d> points, int axisIndex, Channel channel) {

        BinaryVoxels<UnsignedByteBuffer> binaryValues = object.binaryVoxels();
        Voxels<?> voxels = channel.voxels().any();
        BoundingBox box = object.boundingBox();

        Extent extent = voxels.extent();
        Extent extentMask = box.extent();

        BinaryValuesByte binaryValuesMask = binaryValues.binaryValues().asByte();

        // Tracks where are writing to on the output list.
        int pointIndex = 0;

        for (int z = 0; z < extentMask.z(); z++) {

            VoxelBuffer<?> buffer = voxels.slice(z + box.cornerMin().z());
            VoxelBuffer<UnsignedByteBuffer> bufferMask = binaryValues.voxels().slice(z);

            for (int y = 0; y < extentMask.y(); y++) {
                for (int x = 0; x < extentMask.x(); x++) {

                    int offsetMask = extentMask.offset(x, y);

                    if (bufferMask.buffer().getRaw(offsetMask) == binaryValuesMask.getOn()) {

                        int offset =
                                extent.offset(x + box.cornerMin().x(), y + box.cornerMin().y());

                        int gradVal = buffer.getInt(offset) - subtractConstant;

                        modifyOrAddPoint(points, pointIndex, gradVal, axisIndex);
                        pointIndex++;
                    }
                }
            }
        }
        assert (points.size() == pointIndex);
    }

    private static void modifyOrAddPoint(
            List<Point3d> points, int pointIndex, int gradVal, int axisIndex) {
        Point3d out = null;
        if (pointIndex == points.size()) {
            out = new Point3d(0, 0, 0);
            points.add(out);
        } else {
            out = points.get(pointIndex);
        }
        assert (out != null);

        out.setValueByDimension(axisIndex, gradVal);
    }
}
