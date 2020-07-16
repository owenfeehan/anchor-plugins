/*-
 * #%L
 * anchor-plugin-mpp
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

package org.anchoranalysis.plugin.mpp.bean.mark.check;

import java.util.function.Function;
import org.anchoranalysis.anchor.mpp.feature.bean.mark.CheckMark;
import org.anchoranalysis.anchor.mpp.feature.error.CheckException;
import org.anchoranalysis.anchor.mpp.mark.Mark;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.geometry.Point3d;
import org.anchoranalysis.core.geometry.Point3i;
import org.anchoranalysis.feature.nrg.NRGStackWithParams;
import org.anchoranalysis.image.bean.provider.BinaryChnlProvider;
import org.anchoranalysis.image.binary.mask.Mask;

public abstract class CheckMarkBinaryChnl extends CheckMark {

    // START BEAN PROPERTIES
    @BeanField private BinaryChnlProvider binaryChnl;

    @BeanField private boolean acceptOutsideScene = false;
    // END BEAN PROPERTIES

    protected Mask createChnl() throws CheckException {
        try {
            return binaryChnl.create();
        } catch (CreateException e) {
            throw new CheckException(
                    String.format("Cannot create binary image (from provider %s)", binaryChnl), e);
        }
    }

    protected boolean isPointOnBinaryChnl(
            Point3d cp, NRGStackWithParams nrgStack, Function<Point3d, Point3i> deriveFunc)
            throws CheckException {

        if (!nrgStack.getDimensions().contains(cp)) {
            return acceptOutsideScene;
        }

        Mask bi = createChnl();
        return bi.isPointOn(deriveFunc.apply(cp));
    }

    @Override
    public boolean isCompatibleWith(Mark testMark) {
        return true;
    }

    public BinaryChnlProvider getBinaryChnl() {
        return binaryChnl;
    }

    public void setBinaryChnl(BinaryChnlProvider binaryChnl) {
        this.binaryChnl = binaryChnl;
    }
}
