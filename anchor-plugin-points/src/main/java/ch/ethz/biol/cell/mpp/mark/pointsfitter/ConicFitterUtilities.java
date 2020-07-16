/*-
 * #%L
 * anchor-plugin-points
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
/* (C)2020 */
package ch.ethz.biol.cell.mpp.mark.pointsfitter;

import cern.colt.matrix.DoubleMatrix2D;
import cern.colt.matrix.linalg.Algebra;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.anchoranalysis.anchor.mpp.bean.points.fitter.PointsFitterException;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
class ConicFitterUtilities {

    public static DoubleMatrix2D matrixLeftDivide(DoubleMatrix2D num, DoubleMatrix2D dem)
            throws PointsFitterException {
        try {
            return new Algebra().inverse(num).zMult(dem, null);
        } catch (IllegalArgumentException e) {
            throw new PointsFitterException(e);
        }
    }
}
