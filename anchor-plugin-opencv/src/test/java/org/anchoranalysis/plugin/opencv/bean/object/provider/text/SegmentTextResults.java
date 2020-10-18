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
package org.anchoranalysis.plugin.opencv.bean.object.provider.text;

import java.util.Arrays;
import java.util.List;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.anchoranalysis.spatial.extent.Extent;
import org.anchoranalysis.spatial.extent.box.BoundingBox;
import org.anchoranalysis.spatial.point.Point3i;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
class SegmentTextResults {

    private static final BoundingBox BOX1_RGB = boxAt(302, 315, 127, 42);
    private static final BoundingBox BOX1_GRAYSCALE = boxAt(316, 319, 104, 33);

    private static final BoundingBox BOX2_RGB = boxAt(393, 199, 31, 28);
    private static final BoundingBox BOX2_GRAYSCALE = boxAt(394, 199, 27, 27);

    private static final BoundingBox BOX3 = boxAt(438, 310, 78, 40);

    public static List<BoundingBox> rgb() {
        return Arrays.asList(BOX1_RGB, BOX2_RGB, BOX3);
    }

    public static List<BoundingBox> grayscale() {
        return Arrays.asList(BOX1_GRAYSCALE, BOX2_GRAYSCALE, BOX3);
    }

    /** Bounding box at particular point and coordinates */
    private static BoundingBox boxAt(int x, int y, int width, int height) {
        return new BoundingBox(new Point3i(x, y, 0), new Extent(width, height));
    }
}
