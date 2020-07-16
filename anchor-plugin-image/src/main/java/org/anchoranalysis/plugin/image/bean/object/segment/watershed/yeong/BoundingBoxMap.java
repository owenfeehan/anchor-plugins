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
/* (C)2020 */
package org.anchoranalysis.plugin.image.bean.object.segment.watershed.yeong;

import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.core.geometry.Point3i;
import org.anchoranalysis.image.object.ObjectCollection;
import org.anchoranalysis.image.object.ObjectCollectionFactory;
import org.anchoranalysis.image.points.PointRange;
import org.anchoranalysis.image.voxel.box.VoxelBox;

final class BoundingBoxMap {

    private List<PointRange> list = new ArrayList<>();

    private HashMap<Integer, Integer> map = new HashMap<>();

    public int indexForValue(int val) {
        return map.computeIfAbsent(
                val,
                key -> {
                    int idNew = list.size();
                    list.add(null);
                    return idNew;
                });
    }

    public ObjectCollection deriveObjects(VoxelBox<IntBuffer> matS)
            throws OperationFailedException {
        return ObjectCollectionFactory.filterAndMapWithIndexFrom(
                list,
                Objects::nonNull,
                (pointRange, index) -> matS.equalMask(pointRange.deriveBoundingBox(), index + 1));
    }

    public int addPointForValue(Point3i point, int val) {
        int reorderedIndex = indexForValue(val);

        // Add the point to the bounding-box
        addPointToBox(reorderedIndex, point);
        return reorderedIndex;
    }

    /**
     * Get bounding box for a particular index, creating if not already there, and then add a point
     * to the box.
     */
    private void addPointToBox(int indx, Point3i point) {
        PointRange pointRange = list.get(indx);
        if (pointRange == null) {
            pointRange = new PointRange();
            list.set(indx, pointRange);
        }
        pointRange.add(point);
    }
}
