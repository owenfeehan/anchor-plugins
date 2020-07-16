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
/* (C)2020 */
package org.anchoranalysis.plugin.mpp.bean.mark.check;

import java.util.Optional;
import org.anchoranalysis.anchor.mpp.bean.regionmap.RegionMap;
import org.anchoranalysis.anchor.mpp.feature.input.memo.FeatureInputSingleMemo;
import org.anchoranalysis.anchor.mpp.mark.Mark;
import org.anchoranalysis.anchor.mpp.pxlmark.memo.PxlMarkMemoFactory;
import org.anchoranalysis.anchor.mpp.pxlmark.memo.VoxelizedMarkMemo;
import org.anchoranalysis.feature.nrg.NRGStackWithParams;

public class FeatureValueIndividualEnergyGreaterThan
        extends FeatureValueCheckMark<FeatureInputSingleMemo> {

    @Override
    protected FeatureInputSingleMemo createFeatureCalcParams(
            Mark mark, RegionMap regionMap, NRGStackWithParams nrgStack) {
        VoxelizedMarkMemo pmm = PxlMarkMemoFactory.create(mark, nrgStack.getNrgStack(), regionMap);
        return new FeatureInputSingleMemo(pmm, Optional.of(nrgStack));
    }
}
