package org.anchoranalysis.plugin.image.feature.bean.object.collection.intersecting;

import java.util.ArrayList;
import java.util.List;


/*
 * #%L
 * anchor-plugin-image-feature
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
import org.anchoranalysis.feature.cache.ChildCacheName;
import org.anchoranalysis.feature.cache.SessionInput;
import org.anchoranalysis.feature.cache.calculation.ResolvedCalculation;
import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.image.feature.object.input.FeatureInputPairObjects;
import org.anchoranalysis.image.feature.object.input.FeatureInputSingleObject;
import org.anchoranalysis.image.object.ObjectCollection;

public abstract class FeatureIntersectingObjsSingleElem extends FeatureIntersectingObjs {

	
	// START BEAN PROPERTIES
	@BeanField
	private Feature<FeatureInputPairObjects> item;
	// END BEAN PROPERTIES
	
	@Override
	protected double valueFor(SessionInput<FeatureInputSingleObject> params, ResolvedCalculation<ObjectCollection, FeatureInputSingleObject> intersecting)
			throws FeatureCalcException {

		return aggregateResults(
			calcResults(params, intersecting)
		);
	}
	
	protected abstract double aggregateResults( List<Double> results );
	
	private List<Double> calcResults( SessionInput<FeatureInputSingleObject> paramsExst, ResolvedCalculation<ObjectCollection, FeatureInputSingleObject> ccIntersecting ) throws FeatureCalcException {
		
		int size = paramsExst.calc(ccIntersecting).size();
		
		List<Double> results = new ArrayList<>();
		for( int i=0; i<size; i++) {
			
			final int index = i;
			
			double res = paramsExst.forChild().calc(
				item,
				new CalculateIntersecting(ccIntersecting, index),
				new ChildCacheName(FeatureIntersectingObjsSingleElem.class,i)
			);
			results.add(res);
		}
		return results;
	}
	
	public Feature<FeatureInputPairObjects> getItem() {
		return item;
	}

	public void setItem(Feature<FeatureInputPairObjects> item) {
		this.item = item;
	}


}
