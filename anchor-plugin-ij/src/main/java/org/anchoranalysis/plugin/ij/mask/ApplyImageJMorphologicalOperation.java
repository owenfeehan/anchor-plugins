/*-
 * #%L
 * anchor-plugin-ij
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
package org.anchoranalysis.plugin.ij.mask;

import ij.Prefs;
import ij.plugin.filter.Binary;
import java.nio.ByteBuffer;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.image.binary.values.BinaryValues;
import org.anchoranalysis.image.binary.voxel.BinaryVoxels;
import org.anchoranalysis.image.convert.IJWrap;
import org.anchoranalysis.image.extent.Extent;

/**
 * Applies an ImageJ (2D) morphological operation to voxels
 *
 * @author Owen Feehan
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ApplyImageJMorphologicalOperation {

    public static void fill(BinaryVoxels<ByteBuffer> voxels) throws OperationFailedException {
        applyOperation(voxels, "fill", 1);
    }

    public static BinaryVoxels<ByteBuffer> applyOperation(
            BinaryVoxels<ByteBuffer> voxels, String command, int iterations)
            throws OperationFailedException {

        if (!voxels.binaryValues().equals(BinaryValues.getDefault())) {
            throw new OperationFailedException("On byte must be 255, and off byte must be 0");
        }

        Prefs.blackBackground = true;

        Binary plugin = createPlugin(command, voxels.extent());

        for (int i = 0; i < iterations; i++) {
            applyOperation(plugin, voxels);
        }

        return voxels;
    }

    private static Binary createPlugin(String command, Extent extent) {
        Binary plugin = new Binary();
        plugin.setup(command, null);
        plugin.setNPasses(extent.z());
        return plugin;
    }

    private static void applyOperation(Binary plugin, BinaryVoxels<ByteBuffer> voxels) {
        // Are we missing a Z slice?
        voxels.extent()
                .iterateOverZ(z -> plugin.run(IJWrap.imageProcessorByte(voxels.slices(), z)));
    }
}
