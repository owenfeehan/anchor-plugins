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
package org.anchoranalysis.plugin.image.bean.object.segment.reduce;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;
import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.exception.OperationFailedException;
import org.anchoranalysis.core.exception.friendly.AnchorImpossibleSituationException;
import org.anchoranalysis.image.core.merge.ObjectMaskMerger;
import org.anchoranalysis.image.voxel.object.ObjectMask;
import org.anchoranalysis.plugin.image.segment.WithConfidence;

/**
 * Where objects overlap, they are merged together if certain conditions are fulfilled.
 *
 * <p>Conditions:
 *
 * <ul>
 *   <li>if the number of voxels in the lower-confidence element is small (relative to the
 *       higher-confidence object) <b>or</b>
 *   <li>if the confidence of the two elements is similar, then the overlapping element is merged
 *       into the source element, and adopts its confidence.
 * </ul>
 *
 * <p>Otherwise, the overlapping element remains in the list to be reduced (with whatever change to
 * its voxels).
 *
 * <p>This achieves a similar result as {@link ThresholdConfidence} but is typically slower (unless
 * there are very few overlapping objects). However, it offers a greater ability to distinguish
 * overlapping objects of significantly differing confidence.
 *
 * @author Owen Feehan
 */
public class ConditionallyMergeOverlappingObjects extends ReduceElementsGreedy<ObjectMask> {

    // START BEAN PROPERTIES
    /**
     * The maximum size for merging. If a clipped-object has fewer voxels than this (relative to the
     * source object) it is always merged.
     */
    @BeanField @Getter @Setter double overlapThreshold = 0.2;

    /**
     * The maximum difference in confidence for merging. If a clipped-object has a lower difference
     * in confidence than this, it is always merged.
     */
    @BeanField @Getter @Setter double confidenceThreshold = 0.1;
    // END BEAN PROPERTIES

    /** Tracks which objects overlap with other objects, updated as merges/deletions occur. */
    private OverlappingObjectsGraph graph;

    @Override
    protected void init(List<WithConfidence<ObjectMask>> allElements) {
        graph = new OverlappingObjectsGraph(allElements);
    }

    @Override
    protected Predicate<ObjectMask> possibleOverlappingObjects(
            ObjectMask source, Iterable<WithConfidence<ObjectMask>> others) {
        // Finds all elements of others that exist in the graph
        return graph.adjacentVerticesOutgoing(source)::contains;
    }

    @Override
    protected boolean includePossiblyOverlappingObject(ObjectMask source, ObjectMask other) {
        // Include any object that overlaps
        return true;
    }

    @Override
    protected boolean processOverlapping(
            WithConfidence<ObjectMask> source,
            WithConfidence<ObjectMask> overlapping,
            Runnable removeOverlapping,
            Consumer<ObjectMask> changeSource) {

        // Remove any voxels from overlappingObject that are also present in proposedObject
        overlapping.getElement().assignOff().toObject(source.getElement());

        // Remove the edge from the graph that indicates overlap is present
        graph.removeEdge(source.getElement(), overlapping.getElement());

        int numberVoxelsOverlapping = overlapping.getElement().numberVoxelsOn();

        // If there are no longer any voxels left on the object, it is removed from consideration.
        if (numberVoxelsOverlapping == 0) {
            return removeVertex(overlapping.getElement(), removeOverlapping);
        } else if (shouldMerge(numberVoxelsOverlapping, source, overlapping)) {
            return mergeSourceWithOverlap(
                    source.getElement(), overlapping.getElement(), removeOverlapping, changeSource);
        } else {
            // NOTHING TO DO. The object (clipped) remains as an element, but with any overlapping
            // voxels removed.
            return false;
        }
    }

    private boolean removeVertex(ObjectMask overlapping, Runnable removeOverlapping) {
        try {
            graph.removeVertex(overlapping);
        } catch (OperationFailedException e) {
            throw new AnchorImpossibleSituationException();
        }
        removeOverlapping.run();
        return false;
    }

    private boolean mergeSourceWithOverlap(
            ObjectMask source,
            ObjectMask overlapping,
            Runnable removeOverlapping,
            Consumer<ObjectMask> changeSource) {
        ObjectMask merged = ObjectMaskMerger.merge(source, overlapping);
        try {
            graph.mergeVerticesInGraph(source, overlapping, merged);
        } catch (OperationFailedException e) {
            throw new AnchorImpossibleSituationException();
        }
        changeSource.accept(merged);
        removeOverlapping.run();
        return true;
    }

    private boolean shouldMerge(
            int numberVoxelsOverlapping,
            WithConfidence<ObjectMask> source,
            WithConfidence<ObjectMask> overlapping) {
        double overlapScore =
                ScoreHelper.overlapScore(numberVoxelsOverlapping, source.getElement());
        return (overlapScore < overlapThreshold)
                || ScoreHelper.confidenceDifference(source, overlapping) < confidenceThreshold;
    }
}
