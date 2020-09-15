/*-
 * #%L
 * anchor-image-bean
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

package org.anchoranalysis.plugin.image.bean.object.segment.stack;

import org.anchoranalysis.core.concurrency.ConcurrencyPlan;
import org.anchoranalysis.core.concurrency.ConcurrentModelPool;
import org.anchoranalysis.image.bean.nonbean.error.SegmentationFailedException;
import org.anchoranalysis.image.bean.segment.SegmentationBean;
import org.anchoranalysis.image.extent.Extent;
import org.anchoranalysis.image.stack.Stack;

/**
 * A base class for algorithms to segment a stack into one or more objects - using a pool of models
 * (e.g. deep-learning models).
 *
 * @author Owen Feehan
 * @param <T> model-type
 */
public abstract class SegmentStackIntoObjectsPooled<T>
        extends SegmentationBean<SegmentStackIntoObjectsPooled<T>> {

    /**
     * Segments individually using a pool of size 1 just for one stack
     *
     * <p>See {@link #segment(Stack, ConcurrentModelPool)} for more details.
     *
     * @param stack the stack to segment
     * @return a collection of objects with corresponding confidence scores.
     * @throws SegmentationFailedException if anything goes wrong during the segmentation.
     */
    public SegmentedObjects segment(Stack stack) throws SegmentationFailedException {
        return segment(stack, createModelPool(ConcurrencyPlan.singleProcessor(0)));
    }

    /**
     * Creates the model pool (to be used by multiple threads)
     *
     * @param plan the number and types of processors available for concurrent execution
     * @return the newly created model pool
     */
    public abstract ConcurrentModelPool<T> createModelPool(ConcurrencyPlan plan);

    /**
     * Segments a stack to produce an object-collection.
     *
     * <p>Any created objects will always exist inside the stack's {@link Extent}.
     *
     * @param stack the stack to segment
     * @return a collection of objects with corresponding confidence scores.
     * @throws SegmentationFailedException if anything goes wrong during the segmentation.
     */
    public abstract SegmentedObjects segment(Stack stack, ConcurrentModelPool<T> modelPool)
            throws SegmentationFailedException;
}
