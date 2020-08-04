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

package org.anchoranalysis.plugin.image.bean.object.provider.merge;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.Value;
import org.anchoranalysis.core.error.InitException;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.core.geometry.Point3d;
import org.anchoranalysis.core.geometry.Point3i;
import org.anchoranalysis.core.geometry.PointConverter;
import org.anchoranalysis.core.log.Logger;
import org.anchoranalysis.image.binary.voxel.BinaryVoxels;
import org.anchoranalysis.image.binary.voxel.BinaryVoxelsFactory;
import org.anchoranalysis.image.extent.BoundingBox;
import org.anchoranalysis.image.extent.Extent;
import org.anchoranalysis.image.extent.ImageResolution;
import org.anchoranalysis.image.object.ObjectCollection;
import org.anchoranalysis.image.object.ObjectMask;
import org.anchoranalysis.image.object.ops.ObjectMaskMerger;
import org.anchoranalysis.plugin.image.object.merge.condition.AfterCondition;
import org.anchoranalysis.plugin.image.object.merge.condition.BeforeCondition;

/**
 * Naive merge algorithm that merges in a very greedy away as long as certain conditions are
 * fulfilled.
 *
 * @author Owen Feehan
 */
@AllArgsConstructor
class NaiveGreedyMerge {

    private final boolean replaceWithMidpoint;
    private final BeforeCondition beforeCondition;
    private final AfterCondition afterCondition;
    private final Optional<ImageResolution> res;
    private final Logger logger;

    @Value
    private static class MergeParams {
        private int startSrc;
        private int endSrc;
    }

    /**
     * Tries to merge objects (the collection is changed in-place)
     *
     * @param objects the objects to merge
     * @throws OperationFailedException
     */
    public ObjectCollection tryMerge(ObjectCollection objects) throws OperationFailedException {

        List<MergeParams> stack = new ArrayList<>();
        MergeParams mergeParams = new MergeParams(0, 0);

        stack.add(mergeParams);

        while (!stack.isEmpty()) {
            MergeParams params = stack.remove(0);
            tryMergeOnIndices(objects, params, stack);
        }

        return objects;
    }

    /**
     * Tries to merge a particular subset of objects in objects based upon the parameters in
     * mergeParams
     *
     * @param objects the entire set of objects
     * @param mergeParams parameters that determine which objects are considered for merge
     * @param stack the entire list of future parameters to also be considered
     * @throws OperationFailedException
     */
    private void tryMergeOnIndices(
            ObjectCollection objects, MergeParams mergeParams, List<MergeParams> stack)
            throws OperationFailedException {

        try {
            afterCondition.init(logger);
        } catch (InitException e) {
            throw new OperationFailedException(e);
        }

        for (int i = mergeParams.getStartSrc(); i < objects.size(); i++) {
            for (int j = mergeParams.getEndSrc(); j < objects.size(); j++) {

                if (i == j) {
                    continue;
                }

                Optional<ObjectMask> merged = tryMerge(objects.get(i), objects.get(j));

                if (merged.isPresent()) {
                    removeTwoIndices(objects, i, j);
                    objects.add(merged.get());

                    int startPos = Math.max(i - 1, 0);
                    stack.add(new MergeParams(startPos, startPos));

                    // After a succesful merge, we don't try to merge again
                    break;
                }
            }
        }
    }

    private static void removeTwoIndices(ObjectCollection objects, int i, int j) {
        if (i < j) {
            objects.remove(j);
            objects.remove(i);
        } else {
            objects.remove(i);
            objects.remove(j);
        }
    }

    private Optional<ObjectMask> tryMerge(ObjectMask source, ObjectMask destination)
            throws OperationFailedException {

        if (!beforeCondition.accept(source, destination, res)) {
            return Optional.empty();
        }

        // Do merge
        ObjectMask merged = merge(source, destination);

        if (!afterCondition.accept(source, destination, merged, res)) {
            return Optional.empty();
        }

        return Optional.of(merged);
    }

    private ObjectMask merge(ObjectMask source, ObjectMask destination) {
        if (replaceWithMidpoint) {
            Point3i pointNew =
                    PointConverter.intFromDouble(
                            Point3d.midPointBetween(
                                    source.boundingBox().midpoint(),
                                    destination.boundingBox().midpoint()));
            return createSinglePixelObject(pointNew);
        } else {
            return ObjectMaskMerger.merge(source, destination);
        }
    }

    private static ObjectMask createSinglePixelObject(Point3i point) {
        Extent extent = new Extent(1, 1, 1);
        BinaryVoxels<ByteBuffer> voxels = BinaryVoxelsFactory.createEmptyOn(extent);
        return new ObjectMask(new BoundingBox(point, extent), voxels);
    }
}
