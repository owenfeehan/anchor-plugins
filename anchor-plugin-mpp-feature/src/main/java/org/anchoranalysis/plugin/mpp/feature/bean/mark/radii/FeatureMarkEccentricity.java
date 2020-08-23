/*-
 * #%L
 * anchor-plugin-mpp-feature
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

package org.anchoranalysis.plugin.mpp.feature.bean.mark.radii;

import org.anchoranalysis.anchor.mpp.feature.bean.mark.FeatureInputMark;
import org.anchoranalysis.anchor.mpp.feature.bean.mark.FeatureMark;
import org.anchoranalysis.anchor.mpp.mark.Mark;
import org.anchoranalysis.anchor.mpp.mark.conic.ConicBase;
import org.anchoranalysis.anchor.mpp.mark.conic.Ellipsoid;
import org.anchoranalysis.feature.cache.SessionInput;
import org.anchoranalysis.feature.calculate.FeatureCalculationException;
import org.anchoranalysis.image.orientation.Orientation;

public abstract class FeatureMarkEccentricity extends FeatureMark {

    @Override
    public double calculate(SessionInput<FeatureInputMark> input) throws FeatureCalculationException {

        Mark mark = input.get().getMark();

        if (!(mark instanceof ConicBase)) {
            throw new FeatureCalculationException("mark must be of type MarkAbstractRadii");
        }

        double[] radii = ((ConicBase) mark).radiiOrdered();

        if (radii.length == 2) {
            return eccentricityForEllipse(radii);
        } else {

            if (!(mark instanceof Ellipsoid)) {
                throw new FeatureCalculationException("mark must be of type MarkEllipsoid");
            }

            return eccentricityForEllipsoid(radii, ((Ellipsoid) mark).getOrientation());
        }
    }

    /**
     * Calculates eccentricity in 3D-case
     *
     * @param radii an array of length 3, ordered from largest radius to smallest
     * @return the eccentricity
     */
    protected abstract double eccentricityForEllipsoid(double[] radii, Orientation orientation);

    /**
     * Calculates eccentricity in 2D-case
     *
     * @param radii an array of length 2, ordered from largest radius to smallest
     * @return the eccentricity
     */
    private double eccentricityForEllipse(double[] radii) {
        return eccentricity(radii[1], radii[0]);
    }

    /**
     * Calculates eccentricity given two axes
     *
     * @param semiMajorAxis length of semi-major axis
     * @param semiMinorAxis length of semi-minor axis
     * @return the eccentricity
     */
    protected static double eccentricity(double semiMajorAxis, double semiMinorAxis) {
        double ratio = semiMinorAxis / semiMajorAxis;
        return Math.sqrt(1.0 - Math.pow(ratio, 2.0));
    }
}
