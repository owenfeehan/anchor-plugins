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

package org.anchoranalysis.plugin.image.task.bean.labeller;

import java.nio.file.Path;
import java.util.Set;
import org.anchoranalysis.bean.AnchorBean;
import org.anchoranalysis.core.exception.InitializeException;
import org.anchoranalysis.core.exception.OperationFailedException;
import org.anchoranalysis.image.io.stack.input.ProvidesStackInput;
import org.anchoranalysis.io.output.outputter.InputOutputContext;

/**
 * Associates a label with an image.
 *
 * <p>e.g. this can be used to associate labels with images for training or evaluation in a
 * machine-learning problem.
 *
 * @author Owen Feehan
 * @param <T> shared-state
 */
public abstract class ImageLabeller<T> extends AnchorBean<ImageLabeller<T>> {

    /**
     * Should be called once before calling any other methods
     *
     * @param pathForBinding a path that can be used by the labeller to make filePath decisions
     */
    public abstract T initialize(Path pathForBinding) throws InitializeException;

    /**
     * A set of identifiers for all groups that can be outputted by the labeller. Should be callable
     * always.
     */
    public abstract Set<String> allLabels(T initialization);

    /** Determines a particular group-identifier for an input */
    public abstract String labelFor(
            T sharedState, ProvidesStackInput input, InputOutputContext context)
            throws OperationFailedException;
}
