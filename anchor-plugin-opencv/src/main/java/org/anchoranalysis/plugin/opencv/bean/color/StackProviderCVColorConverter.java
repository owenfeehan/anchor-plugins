/* (C)2020 */
package org.anchoranalysis.plugin.opencv.bean.color;

import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.image.bean.provider.stack.StackProviderOne;
import org.anchoranalysis.image.extent.Extent;
import org.anchoranalysis.image.stack.Stack;
import org.anchoranalysis.plugin.opencv.CVInit;
import org.anchoranalysis.plugin.opencv.MatConverter;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

/**
 * Converts a RGB stack into another color space using OpenCV
 *
 * <p>Note: there might be quite a bit of redundant memory allocation here as the Java ByteArrays
 * aren't directly usable in OpenCV and vice-versa, so new images are created both inwards and
 * outwards.
 *
 * <p>TODO: find a way to use the same allocated memory in both the Java structures and the OpenCV
 * structures
 *
 * @author Owen Feehan
 */
public abstract class StackProviderCVColorConverter extends StackProviderOne {

    static {
        CVInit.alwaysExecuteBeforeCallingLibrary();
    }

    @Override
    public Stack createFromStack(Stack stackRGB) throws CreateException {

        checkNumChnls(stackRGB);

        Mat matBGR = MatConverter.makeRGBStack(stackRGB);

        Mat matHSV =
                convertColorSpace(stackRGB.getDimensions().getExtent(), matBGR, colorSpaceCode());

        return createOutputStack(stackRGB, matHSV);
    }

    /**
     * The color space conversion code to use from OpenCV
     *
     * <p>Assume that the inputted image is provided is a 3 channel stack in BGR order
     */
    protected abstract int colorSpaceCode();

    private static Mat convertColorSpace(Extent e, Mat matBGR, int code) {
        Mat matHSV = MatConverter.createEmptyMat(e, CvType.CV_8UC3);
        Imgproc.cvtColor(matBGR, matHSV, code);
        return matHSV;
    }

    private void checkNumChnls(Stack stack) throws CreateException {
        if (stack.getNumChnl() != 3) {
            throw new CreateException(
                    "Input stack must have exactly 3 channels representing a RGB image");
        }
    }

    private static Stack createOutputStack(Stack stackIn, Mat matOut) throws CreateException {
        Stack stackOut = stackIn.duplicate();
        MatConverter.matToRGB(matOut, stackOut);
        return stackOut;
    }
}
