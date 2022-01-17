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
package org.anchoranalysis.plugin.opencv.bean.stack;

import com.google.common.base.CharMatcher;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.anchoranalysis.core.exception.OperationFailedException;
import org.anchoranalysis.core.functional.OptionalFactory;
import org.anchoranalysis.core.log.Logger;
import org.anchoranalysis.core.progress.Progress;
import org.anchoranalysis.core.time.ExecutionTimeRecorder;
import org.anchoranalysis.image.core.dimensions.Dimensions;
import org.anchoranalysis.image.core.stack.RGBChannelNames;
import org.anchoranalysis.image.core.stack.Stack;
import org.anchoranalysis.image.io.ImageIOException;
import org.anchoranalysis.image.io.stack.input.ImageTimestampsAttributes;
import org.anchoranalysis.image.io.stack.input.OpenedImageFile;
import org.anchoranalysis.image.io.stack.time.TimeSequence;
import org.anchoranalysis.io.bioformats.metadata.ImageTimestampsAttributesFactory;
import org.anchoranalysis.plugin.opencv.convert.ConvertFromMat;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.imgcodecs.Imgcodecs;

/**
 * An opened-image file using the OpenCV library.
 *
 * <p>Note that when <i>reading image metadata</i> only, this is computionally slow, as the entire
 * image must be loaded into memory to determine the width and height. Users are recommended to use
 * another library for this purpose.
 *
 * <p>However, unlike many other libraries, OpenCV has the advantage of automatically correcting the
 * orientation (to give correct widths and heights) where EXIF rotation information is present.
 *
 * <p>When a file-path contains non-ASCII characters, then a slower method must be used to open
 * files (approximately 4 times slower) than when a path contains only ASCII characters.
 *
 * @author Owen Feehan
 */
@RequiredArgsConstructor
class OpenedRasterOpenCV implements OpenedImageFile {

    // START REQUIRED ARGUMENTS
    /**
     * The path to open.
     *
     * <p>Note that only ascii paths are supported by OpenCV, <a
     * href="https://stackoverflow.com/questions/24769623/opencv-imread-on-windows-for-non-ascii-file-names">unicode
     * paths are not supported</a>.
     */
    private final Path path;

    /** Records the execution time of operations. */
    private final ExecutionTimeRecorder executionTimeRecorder;
    // END REQUIRED ARGUMENTS

    /** Lazily opened stack. */
    private Stack stack;

    /** Lazily recorded timestamps. */
    private ImageTimestampsAttributes timestamps;

    @Override
    public TimeSequence open(int seriesIndex, Progress progress, Logger logger)
            throws ImageIOException {
        openStackIfNecessary();
        return new TimeSequence(stack);
    }

    @Override
    public int numberSeries() {
        return 1;
    }

    @Override
    public Optional<List<String>> channelNames(Logger logger) throws ImageIOException {
        openStackIfNecessary();
        boolean includeAlpha = numberChannels(logger) == 4;
        return OptionalFactory.create(stack.isRGB(), RGBChannelNames.asList(includeAlpha));
    }

    @Override
    public int numberChannels(Logger logger) throws ImageIOException {
        openStackIfNecessary();
        return stack.getNumberChannels();
    }

    @Override
    public int numberFrames(Logger logger) throws ImageIOException {
        return 1;
    }

    @Override
    public int bitDepth(Logger logger) throws ImageIOException {
        openStackIfNecessary();
        if (!stack.allChannelsHaveIdenticalType()) {
            throw new ImageIOException(
                    "Not all channels have identical channel type, so not calculating bit-depth.");
        }
        return stack.getChannel(0).getVoxelDataType().bitDepth();
    }

    @Override
    public boolean isRGB() throws ImageIOException {
        openStackIfNecessary();
        return stack.isRGB();
    }

    @Override
    public void close() throws ImageIOException {
        stack = null;
    }

    @Override
    public Dimensions dimensionsForSeries(int seriesIndex, Logger logger) throws ImageIOException {
        openStackIfNecessary();
        return stack.dimensions();
    }

    @Override
    public ImageTimestampsAttributes timestamps() throws ImageIOException {
        if (timestamps == null) {
            timestamps = ImageTimestampsAttributesFactory.fromPath(path);
        }
        return timestamps;
    }

    /** Opens the stack if has not already been opened. */
    private void openStackIfNecessary() throws ImageIOException {
        if (stack == null) {
            try {
                Mat image = readDecodeMat(path, executionTimeRecorder);
                stack =
                        executionTimeRecorder.recordExecutionTime(
                                "Convert OpenCV to stack", () -> ConvertFromMat.toStack(image));
            } catch (OperationFailedException | IOException e) {
                throw new ImageIOException(
                        "Failed to convert an OpenCV image structure to a stack", e);
            }
        }
    }

    /**
     * Reads an image at {@code path} and decodes into a {@link Mat}.
     *
     * <p>Two methods are used, in order of preference:
     *
     * <ol>
     *   <li>Using {@link Imgcodecs#imread} where possible (it only supports paths with ASCII
     *       characters) as it is much more efficient.
     *   <li>Using {@link Imgcodecs.imdecode} on a byte-array. This is slower as it involves loading
     *       all bytes in the JVM and moving back into native code, and then back into the JVM.
     * </ol>
     *
     * <p>The second method seems to be approximately 4 times slower empirically.
     *
     * <p>See this <a
     * href="https://stackoverflow.com/questions/43185605/how-do-i-read-an-image-from-a-path-with-unicode-characters">Stack
     * Overflow post</a> for more details on the problem/
     *
     * @param path the path to read the file from.
     * @param recorder records execution times.
     * @return the image read from the file-system.
     * @throws IOException if the bytes for the file cannot be read form the file-system (when a
     *     non-ascii path).
     */
    private Mat readDecodeMat(Path path, ExecutionTimeRecorder recorder) throws IOException {
        String pathAsString = path.toString();
        boolean isAscii = CharMatcher.ascii().matchesAllOf(pathAsString);
        if (isAscii) {
            return recorder.recordExecutionTime(
                    "imread with OpenCV", () -> Imgcodecs.imread(pathAsString));
        } else {
            byte[] bytes =
                    recorder.recordExecutionTime(
                            "Reading file bytes (non-ascii path)", () -> Files.readAllBytes(path));
            Mat mat = new MatOfByte(bytes);
            return recorder.recordExecutionTime(
                    "imgdecode with OpenCV (non-ascii path)",
                    () -> Imgcodecs.imdecode(mat, Imgcodecs.IMREAD_UNCHANGED));
        }
    }
}
