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

package org.anchoranalysis.plugin.mpp.feature.bean.mark.direction;

import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.anchor.mpp.feature.bean.mark.FeatureInputMark;
import org.anchoranalysis.anchor.mpp.feature.bean.mark.FeatureMark;
import org.anchoranalysis.anchor.mpp.mark.conic.Ellipsoid;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.error.InitException;
import org.anchoranalysis.core.geometry.Vector3d;
import org.anchoranalysis.feature.cache.SessionInput;
import org.anchoranalysis.feature.calculate.FeatureCalculationException;
import org.anchoranalysis.feature.calculate.FeatureInitParams;
import org.anchoranalysis.image.bean.orientation.VectorInDirection;
import org.anchoranalysis.image.orientation.Orientation;
import org.anchoranalysis.math.rotation.RotationMatrix;

public abstract class FeatureMarkDirection extends FeatureMark {

    // START BEAN PROPERTIES
    @BeanField @Getter @Setter private VectorInDirection direction;
    // END BEAN PROPERTIES

    private Vector3d vectorInDirection;

    @Override
    protected void beforeCalc(FeatureInitParams paramsInit) throws InitException {
        super.beforeCalc(paramsInit);
        this.vectorInDirection = direction.createVector().createVector3d();
    }

    @Override
    public double calculate(SessionInput<FeatureInputMark> input) throws FeatureCalculationException {

        if (!(input.get().getMark() instanceof Ellipsoid)) {
            throw new FeatureCalculationException("Only supports MarkEllipsoids");
        }

        Ellipsoid mark = (Ellipsoid) input.get().getMark();

        Orientation orientation = mark.getOrientation();
        return calculateForEllipsoid(mark, orientation, orientation.createRotationMatrix(), vectorInDirection);
    }

    protected abstract double calculateForEllipsoid(
            Ellipsoid mark,
            Orientation orientation,
            RotationMatrix rotMatrix,
            Vector3d directionVector)
            throws FeatureCalculationException;
}
