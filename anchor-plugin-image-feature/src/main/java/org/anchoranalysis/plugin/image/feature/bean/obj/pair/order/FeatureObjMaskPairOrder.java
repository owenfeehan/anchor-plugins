package org.anchoranalysis.plugin.image.feature.bean.obj.pair.order;

/*-
 * #%L
 * anchor-plugin-image-feature
 * %%
 * Copyright (C) 2010 - 2020 Owen Feehan
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
import org.anchoranalysis.feature.cache.SessionInput;
import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.image.feature.bean.objmask.pair.FeatureObjMaskPair;
import org.anchoranalysis.image.feature.objmask.FeatureInputSingleObj;
import org.anchoranalysis.image.feature.objmask.pair.FeatureInputPairObjs;

/**
 * Base class for evaluating FeatureObjMaskPair in terms of the order of the elements (first object, second order etc.)
 * 
 * @author owen
 *
 */
public abstract class FeatureObjMaskPairOrder extends FeatureObjMaskPair {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	// START BEAN PROPERTIES
	@BeanField
	private Feature<FeatureInputSingleObj> feature;
	// END BEAN PROPERTIES
		
	protected double valueFromObj1( SessionInput<FeatureInputPairObjs> params ) throws FeatureCalcException {
		return featureValFrom(
			params,
			true,
			"firstFromPair"
		);
	}
	
	protected double valueFromObj2( SessionInput<FeatureInputPairObjs> params ) throws FeatureCalcException {
		return featureValFrom(
			params,
			false,
			"secondFromPair"
		);
	}
	
	private double featureValFrom( SessionInput<FeatureInputPairObjs> params, boolean first, String sessionName ) throws FeatureCalcException {
	
		return params.calcChild(
			feature,
			new CalculateParamsFromPair(first),
			sessionName
		);
	}
	
	public Feature<FeatureInputSingleObj> getFeature() {
		return feature;
	}

	public void setFeature(Feature<FeatureInputSingleObj> feature) {
		this.feature = feature;
	}
}
