package ch.ethz.biol.cell.mpp.mark.check;

import org.anchoranalysis.anchor.mpp.bean.regionmap.RegionMap;
import org.anchoranalysis.anchor.mpp.feature.nrg.elem.NRGElemIndCalcParams;
import org.anchoranalysis.anchor.mpp.mark.Mark;
import org.anchoranalysis.anchor.mpp.pxlmark.memo.PxlMarkMemo;
import org.anchoranalysis.anchor.mpp.pxlmark.memo.PxlMarkMemoFactory;

/*
 * #%L
 * anchor-plugin-mpp
 * %%
 * Copyright (C) 2016 ETH Zurich, University of Zurich, Owen Feehan
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


import org.anchoranalysis.feature.nrg.NRGStackWithParams;

public class FeatureValueIndividualEnergyGreaterThan extends FeatureValueCheckMark<NRGElemIndCalcParams> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1086432241915811567L;

	@Override
	protected NRGElemIndCalcParams createFeatureCalcParams(Mark mark, RegionMap regionMap, NRGStackWithParams nrgStack) {
		PxlMarkMemo pmm = PxlMarkMemoFactory.create( mark, nrgStack.getNrgStack(), regionMap );
		return new NRGElemIndCalcParams(pmm, nrgStack);
	}
	
	



}
