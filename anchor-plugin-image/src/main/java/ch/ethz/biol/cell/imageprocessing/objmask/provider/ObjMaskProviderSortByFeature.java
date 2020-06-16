package ch.ethz.biol.cell.imageprocessing.objmask.provider;

/*
 * #%L
 * anchor-plugin-image
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


import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.feature.session.calculator.FeatureCalculatorSingle;
import org.anchoranalysis.image.bean.provider.ObjMaskProviderOne;
import org.anchoranalysis.image.feature.bean.evaluator.FeatureEvaluator;
import org.anchoranalysis.image.feature.objmask.FeatureInputSingleObj;
import org.anchoranalysis.image.objectmask.ObjectMask;
import org.anchoranalysis.image.objectmask.ObjectCollection;

public class ObjMaskProviderSortByFeature extends ObjMaskProviderOne {

	// START BEAN PROPERTIES
	@BeanField
	private FeatureEvaluator<FeatureInputSingleObj> featureEvaluator;
	// END BEAN PROPERTIES
	
	private static class ObjWithFeatureValue implements Comparable<ObjWithFeatureValue> {
		
		private ObjectMask objMask;
		private double featureVal;
		
		public ObjWithFeatureValue(ObjectMask objMask, double featureVal) throws FeatureCalcException {
			super();
			this.objMask = objMask;
			this.featureVal = featureVal;
		}

		@Override
		public int compareTo(ObjWithFeatureValue o) {
			return Double.valueOf(o.featureVal).compareTo(featureVal);
		}

		public ObjectMask getObjMask() {
			return objMask;
		}

		
	}
	
	@Override
	public ObjectCollection createFromObjs( ObjectCollection objsCollection ) throws CreateException {
		
		try {
			FeatureCalculatorSingle<FeatureInputSingleObj> featureSession = featureEvaluator.createAndStartSession();
			
			List<ObjWithFeatureValue> listToSort = new ArrayList<>();
			for( ObjectMask om : objsCollection ) {
				try {
					double featureVal = featureSession.calc(
						new FeatureInputSingleObj(om)
					);
					listToSort.add( new ObjWithFeatureValue(om,featureVal) );
				} catch (FeatureCalcException e) {
					throw new CreateException(e);
				}
			}
			
			Collections.sort(listToSort);
					
			ObjectCollection objsOut = new ObjectCollection();
			for( ObjWithFeatureValue om : listToSort ) {
				objsOut.add(om.getObjMask());
			}
			
			return objsOut;
			
		} catch (OperationFailedException e) {
			throw new CreateException(e);
		}
	}

	public FeatureEvaluator<FeatureInputSingleObj> getFeatureEvaluator() {
		return featureEvaluator;
	}

	public void setFeatureEvaluator(FeatureEvaluator<FeatureInputSingleObj> featureEvaluator) {
		this.featureEvaluator = featureEvaluator;
	}
}
