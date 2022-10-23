/*-
 * #%L
 * anchor-plugin-image-task
 * %%
 * Copyright (C) 2010 - 2022 Owen Feehan, ETH Zurich, University of Zurich, Hoffmann-La Roche
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
package org.anchoranalysis.plugin.image.task.bean.scale;

import java.util.List;
import java.util.Optional;
import org.anchoranalysis.core.exception.OperationFailedException;
import org.anchoranalysis.experiment.arguments.TaskArguments;
import org.anchoranalysis.image.core.dimensions.size.suggestion.ImageSizeSuggestion;
import org.anchoranalysis.image.core.dimensions.size.suggestion.ImageSizeSuggestionFactory;
import org.anchoranalysis.image.core.dimensions.size.suggestion.SuggestionFormatException;
import org.anchoranalysis.image.io.ImageIOException;
import org.anchoranalysis.image.io.stack.input.StackSequenceInput;
import org.anchoranalysis.plugin.image.bean.scale.ToSuggested;
import org.anchoranalysis.plugin.image.task.bean.ColoredStacksInputFixture;
import org.anchoranalysis.plugin.image.task.bean.StackIOTestBase;
import org.anchoranalysis.test.experiment.task.ExecuteTaskHelper;
import org.anchoranalysis.test.image.io.BeanInstanceMapFixture;
import org.junit.jupiter.api.Test;

class ScaleImageIndependentlyTest extends StackIOTestBase {

    /** Fixed width. */
    @Test
    void testFixedWidth()
            throws OperationFailedException, ImageIOException, SuggestionFormatException {
        ImageSizeSuggestion suggestion = ImageSizeSuggestionFactory.create("800x");
        doTest(false, "fixedWidth", suggestion);
    }

    @SuppressWarnings("unchecked")
    private void doTest(
            boolean binary, String expectedOutputSubdirectory, ImageSizeSuggestion suggestion)
            throws ImageIOException, OperationFailedException, SuggestionFormatException {

        ScaleImageIndependently task = new ScaleImageIndependently();
        task.setBinary(binary);

        task.setScaleCalculator(new ToSuggested());

        BeanInstanceMapFixture.check(task);

        ExecuteTaskHelper.runTaskAndCompareOutputs(
                (List<StackSequenceInput>)
                        ColoredStacksInputFixture.createInputs(STACK_READER, false),
                true,
                task,
                new TaskArguments(Optional.of(suggestion)),
                directory,
                Optional.of(ScaleImage.OUTPUT_SCALED),
                "scaleImageIndependently/expectedOutput/nonBinary/" + expectedOutputSubdirectory,
                ColoredStacksInputFixture.FILENAMES_WITH_EXTENSION);
    }
}
