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

package org.anchoranalysis.plugin.ij.bean.object.segment;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.anchoranalysis.image.convert.ByteConverter;
import org.anchoranalysis.image.extent.BoundingBox;
import org.anchoranalysis.image.object.ObjectCollection;
import org.anchoranalysis.image.object.ObjectCollectionFactory;
import org.anchoranalysis.image.points.PointRange;
import org.anchoranalysis.image.voxel.Voxels;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access=AccessLevel.PRIVATE)
class CreateFromLabels {

    /**
     * Creates an object-collection from voxels that are labelled with unique integers
     * (sequentially increasing)
     *
     * @param voxels voxels each labelled with an integer (sequentially increasing from 1) to
     *     represent an object
     * @param minLabel minimum label-value inclusive
     * @param maxLabel maximum label-value inclusive
     * @param minBBoxVolume
     * @return
     */
    public static ObjectCollection create(
            Voxels<ByteBuffer> voxels, int minLabel, int maxLabel, int minBBoxVolume) {
        int[] numPixel = new int[maxLabel - minLabel + 1];

        return createFromLabels(
                calcBoundingBoxes(voxels, minLabel, maxLabel, numPixel), voxels, minBBoxVolume);
    }

    private static List<BoundingBox> calcBoundingBoxes(
            Voxels<ByteBuffer> voxels, int minC, int maxC, int[] numPixel) {

        List<PointRange> list = new ArrayList<>(maxC - minC + 1);

        for (int i = minC; i <= maxC; i++) {
            list.add(new PointRange());
        }

        for (int z = 0; z < voxels.getPlaneAccess().extent().z(); z++) {

            ByteBuffer pixel = voxels.getPlaneAccess().getPixelsForPlane(z).buffer();

            for (int y = 0; y < voxels.getPlaneAccess().extent().y(); y++) {
                for (int x = 0; x < voxels.getPlaneAccess().extent().x(); x++) {

                    int col = ByteConverter.unsignedByteToInt(pixel.get());

                    if (col == 0) {
                        continue;
                    }

                    list.get(col - 1).add(x, y, z);
                    numPixel[col - 1]++;
                }
            }
        }

        /** Convert to bounding-boxes after filtering any empty point-ranges */
        return list.stream()
                .filter(p -> !p.isEmpty())
                .map(PointRange::deriveBoundingBoxNoCheck)
                .collect(Collectors.toList());
    }

    private static ObjectCollection createFromLabels(
            List<BoundingBox> boxList,
            Voxels<ByteBuffer> voxels,
            int smallVolumeThreshold) {
        return ObjectCollectionFactory.filterAndMapWithIndexFrom(
                boxList,
                box -> box.extent().volumeXY() >= smallVolumeThreshold,
                voxels::equalMask // Using index as the color value
                );
    }
}
