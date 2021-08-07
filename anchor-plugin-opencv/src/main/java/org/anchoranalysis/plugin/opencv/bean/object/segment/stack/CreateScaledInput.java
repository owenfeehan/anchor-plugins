/*-
 * #%L
 * anchor-plugin-opencv
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

package org.anchoranalysis.plugin.opencv.bean.object.segment.stack;

import io.vavr.Tuple;
import io.vavr.Tuple2;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.anchoranalysis.core.exception.CreateException;
import org.anchoranalysis.image.core.dimensions.size.ResizeExtentUtilities;
import org.anchoranalysis.image.core.stack.Stack;
import org.anchoranalysis.plugin.opencv.convert.ConvertToMat;
import org.anchoranalysis.spatial.Extent;
import org.anchoranalysis.spatial.scale.ScaleFactor;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

/**
 * Creates a scaled-version of a stack to use as input
 *
 * @author Owen Feehan
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
class CreateScaledInput {

    /**
     * Returns a scaled-down version of the stack, and a scale-factor that would return it to
     * original size
     *
     * @param swapRB if true, the first channel and third channel in {@code stack} are swapped to
     *     make the {@link Mat} to e.g. translate RGB to BGR (as expected by OpenCV).
     */
    public static Tuple2<Mat, ScaleFactor> apply(Stack stack, Extent targetExtent, boolean swapRB)
            throws CreateException {

        Mat original = ConvertToMat.makeRGBStack(stack, swapRB);

        Mat input = resizeMatToTarget(original, targetExtent);
        return Tuple.of(input, relativeScale(original, input));
    }

    private static ScaleFactor relativeScale(Mat original, Mat resized) {
        return ResizeExtentUtilities.relativeScale(extentFromMat(resized), extentFromMat(original));
    }

    private static Extent extentFromMat(Mat mat) {
        return new Extent(mat.cols(), mat.rows());
    }

    private static Mat resizeMatToTarget(Mat src, Extent targetExtent) {
        Mat destination = new Mat();
        Size size = new Size(targetExtent.x(), targetExtent.y());
        Imgproc.resize(src, destination, size);
        return destination;
    }
}
