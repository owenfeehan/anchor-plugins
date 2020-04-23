package ch.ethz.biol.cell.mpp.nrg.feature.all;

import org.anchoranalysis.anchor.mpp.feature.bean.nrg.elem.NRGElemAll;
import org.anchoranalysis.anchor.mpp.feature.input.memo.FeatureInputAllMemo;
import org.anchoranalysis.anchor.mpp.feature.input.memo.FeatureInputSingleMemo;
import org.anchoranalysis.anchor.mpp.feature.mark.MemoCollection;

/*
 * #%L
 * anchor-plugin-mpp-feature
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


import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.feature.bean.Feature;
import org.anchoranalysis.feature.cache.CacheableParams;
import org.anchoranalysis.feature.calc.FeatureCalcException;

public class MeanFromAll extends NRGElemAll {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	// START BEAN PROPERTIES
	@BeanField
	private Feature<FeatureInputSingleMemo> item;
	// END BEAN PROPERTIES
	
	@Override
	public double calc(CacheableParams<FeatureInputAllMemo> paramsCacheable)
			throws FeatureCalcException {
		
		MemoCollection memo = paramsCacheable.getParams().getPxlPartMemo();
		
		if (memo.size()==0) {
			return 0.0;
		}
		
		double sum = 0.0;
		
		for( int i=0; i<memo.size(); i++) {

			final int index = i;
			
			sum += paramsCacheable.calcChangeParams(
				item,
				p -> paramsForInd(p, index),
				"obj_" + i
			);
		}
		
		return sum / memo.size();
	}
	
	private static FeatureInputSingleMemo paramsForInd( FeatureInputAllMemo params, int index ) {
		FeatureInputSingleMemo paramsInd = new FeatureInputSingleMemo(
			null,
			params.getNrgStack()
		);
		paramsInd.setPxlPartMemo(
			params.getPxlPartMemo().getMemoForIndex(index)
		);
		return paramsInd;
	}

	public Feature<FeatureInputSingleMemo> getItem() {
		return item;
	}

	public void setItem(Feature<FeatureInputSingleMemo> item) {
		this.item = item;
	}
}
