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

package org.anchoranalysis.plugin.opencv.bean.color;

import org.anchoranalysis.bean.xml.exception.ProvisionFailedException;
import org.anchoranalysis.core.exception.CreateException;
import org.anchoranalysis.core.exception.OperationFailedException;
import org.anchoranalysis.image.bean.provider.stack.StackProviderUnary;
import org.anchoranalysis.image.core.stack.Stack;
import org.anchoranalysis.plugin.opencv.CVInit;
import org.anchoranalysis.plugin.opencv.convert.ConvertFromMat;
import org.anchoranalysis.plugin.opencv.convert.ConvertToMat;
import org.anchoranalysis.spatial.box.Extent;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

/**
 * Converts a RGB stack into another color space using OpenCV.
 *
 * <p>Note that internally the RGB image is reordered as BGR, as OpenCV expects this order, but this
 * should not be performed externally by the user when calling this class, it is rather
 * automatically taken care of.
 *
 * <p>i.e. please always pass the channels in a {@link Stack} in RGB order.
 *
 * @author Owen Feehan
 */
public abstract class ColorConverterBase extends StackProviderUnary {

    static {
        CVInit.alwaysExecuteBeforeCallingLibrary();
    }

    @Override
    public Stack createFromStack(Stack stackRGB) throws ProvisionFailedException {

        checkNumberChannels(stackRGB);

        CVInit.blockUntilLoaded();

        try {
            Mat matBGR = ConvertToMat.makeRGBStack(stackRGB, true);

            Mat matHSV = convertColorSpace(stackRGB.extent(), matBGR, colorSpaceCode());

            return ConvertFromMat.toStack(matHSV);
        } catch (OperationFailedException | CreateException e) {
            throw new ProvisionFailedException(e);
        }
    }

    /**
     * The color space conversion code to use from OpenCV, that defines the color-space to convert
     * to.
     *
     * <p>Assume that the inputted image is provided is a 3 channel stack in BGR order.
     *
     * @return the code, as defined by OpenCV, to describe the target color-space.
     */
    protected abstract int colorSpaceCode();

    private static Mat convertColorSpace(Extent extent, Mat matBGR, int code) {
        Mat matHSV = ConvertToMat.createEmptyMat(extent, CvType.CV_8UC3);
        Imgproc.cvtColor(matBGR, matHSV, code);
        return matHSV;
    }

    private void checkNumberChannels(Stack stack) throws ProvisionFailedException {
        if (stack.getNumberChannels() != 3) {
            throw new ProvisionFailedException(
                    "Input stack must have exactly 3 channels representing a RGB image");
        }
    }
}
