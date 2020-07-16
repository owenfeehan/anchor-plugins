/*-
 * #%L
 * anchor-plugin-image-feature
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

package org.anchoranalysis.plugin.image.feature.bean.pixelscore;

import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.image.histogram.Histogram;

public class PixelScoreCalculateLevel extends PixelScoreCalculateLevelBase {

    // START BEAN PROPERTIES
    @BeanField @Getter @Setter private double distanceMax = 20;
    // END BEAN PROPERTIES

    private double distanceMaxDivider;

    @Override
    protected void beforeCalcSetup(Histogram hist, int level) {
        // We divide by twice the distanceMax so we always get a figure bounded [0,0.5]
        distanceMaxDivider = distanceMax * 2;
    }

    @Override
    protected double calcForPixel(int pxlValue, int level) {

        if (pxlValue < level) {

            int diff = level - pxlValue;

            if (diff > distanceMax) {
                return 0;
            }

            double mem = diff / distanceMaxDivider;
            return 0.5 - mem;
        } else {
            int diff = pxlValue - level;

            if (diff > distanceMax) {
                return 1;
            }

            double mem = diff / distanceMaxDivider;
            return 0.5 + mem;
        }
    }
}
