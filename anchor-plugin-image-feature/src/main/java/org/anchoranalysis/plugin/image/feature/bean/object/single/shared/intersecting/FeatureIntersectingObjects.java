package org.anchoranalysis.plugin.image.feature.bean.object.single.shared.intersecting;

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
import org.anchoranalysis.core.error.InitException;
import org.anchoranalysis.core.name.provider.NamedProviderGetException;
import org.anchoranalysis.feature.cache.SessionInput;
import org.anchoranalysis.feature.cache.calculation.ResolvedCalculation;
import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.feature.calc.FeatureInitParams;
import org.anchoranalysis.image.bean.nonbean.init.ImageInitParams;
import org.anchoranalysis.image.feature.bean.object.single.FeatureSingleObject;
import org.anchoranalysis.image.feature.object.input.FeatureInputSingleObject;
import org.anchoranalysis.image.object.ObjectCollection;

import lombok.Getter;
import lombok.Setter;

public abstract class FeatureIntersectingObjects extends FeatureSingleObject {

	
	// START BEAN PROPERTIES
	/**
	 * ID for the particular ObjMaskCollection
	 */
	@BeanField @Getter @Setter
	private String id="";
	
	@BeanField @Getter @Setter
	private double valueNoObjects = Double.NaN;
	// END BEAN PROPERTIES

	private ObjectCollection searchObjs;
	
	@Override
	protected void beforeCalc(FeatureInitParams paramsInit) throws InitException {
		super.beforeCalc(paramsInit);
		
		ImageInitParams imageInit = new ImageInitParams(paramsInit.sharedObjectsRequired());
		try {
			this.searchObjs = imageInit.getObjMaskCollection().getException(id);
		} catch (NamedProviderGetException e) {
			throw new InitException(e.summarize());
		}

	}
		
	@Override
	public double calc(SessionInput<FeatureInputSingleObject> input)
			throws FeatureCalcException {

		if (getSearchObjs().size()==0) {
			return getValueNoObjects();
		}
		
		return valueFor(
			input,
			input.resolver().search(
				new CalculateIntersectingObjects(id, searchObjs)
			)
		);
	}
	
	protected abstract double valueFor(
		SessionInput<FeatureInputSingleObject> params,
		ResolvedCalculation<ObjectCollection, FeatureInputSingleObject> intersecting
	) throws FeatureCalcException;

	protected ObjectCollection getSearchObjs() {
		return searchObjs;
	}
}
