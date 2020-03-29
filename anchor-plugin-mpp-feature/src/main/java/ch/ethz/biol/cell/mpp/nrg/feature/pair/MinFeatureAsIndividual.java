package ch.ethz.biol.cell.mpp.nrg.feature.pair;

import org.anchoranalysis.anchor.mpp.feature.bean.nrg.elem.NRGElemPair;
import org.anchoranalysis.anchor.mpp.feature.nrg.elem.NRGElemIndCalcParams;
import org.anchoranalysis.anchor.mpp.feature.nrg.elem.NRGElemPairCalcParams;

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
import org.anchoranalysis.feature.calc.FeatureCalcException;

public class MinFeatureAsIndividual extends NRGElemPair {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	// START BEAN PROPERTIES
	@BeanField
	private Feature item;
	// eND BEAN PROPERTIES
	
	public MinFeatureAsIndividual() {
	}
	
	@Override
	public double calcCast( NRGElemPairCalcParams params ) throws FeatureCalcException {
		
		NRGElemIndCalcParams params1 = new NRGElemIndCalcParams( params.getObj1(), params.getNrgStack() );
		NRGElemIndCalcParams params2 = new NRGElemIndCalcParams( params.getObj2(), params.getNrgStack() );
		
		double val1 = getCacheSession().calc( item, params1 );
		double val2 = getCacheSession().calc( item, params2 );
		return Math.min(val1, val2);
	}

	public Feature getItem() {
		return item;
	}

	public void setItem(Feature item) {
		this.item = item;
	}
}
