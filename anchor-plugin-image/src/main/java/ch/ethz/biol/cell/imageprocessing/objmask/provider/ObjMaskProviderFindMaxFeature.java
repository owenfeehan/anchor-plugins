package ch.ethz.biol.cell.imageprocessing.objmask.provider;

import java.util.Optional;

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


import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.feature.session.calculator.FeatureCalculatorSingle;
import org.anchoranalysis.image.objectmask.ObjectMask;
import org.anchoranalysis.image.feature.object.input.FeatureInputSingleObject;
import org.anchoranalysis.image.objectmask.ObjectCollection;
import org.anchoranalysis.image.objectmask.ObjectCollectionFactory;

public class ObjMaskProviderFindMaxFeature extends ObjMaskProviderFindMaxFeatureBase {

	@Override
	public ObjectCollection createFromObjs( ObjectCollection objMaskCollection ) throws CreateException {

		Optional<ObjectMask> max = findMaxObj(
			createSession(),
			objMaskCollection
		);
		
		if (max.isPresent()) {
			return ObjectCollectionFactory.from(max.get());
		} else {
			return ObjectCollectionFactory.empty();
		}
	}
	
	private Optional<ObjectMask> findMaxObj( FeatureCalculatorSingle<FeatureInputSingleObject> session, ObjectCollection in ) throws CreateException {
		
		try {
			ObjectMask max = null;
			
			double maxVal = 0;
			for( ObjectMask om : in ) {
				
				double featureVal = session.calc(
					new FeatureInputSingleObject(om)
				);
				
				if (max==null || featureVal>maxVal) {
					max = om;
					maxVal = featureVal;
				}
			}
			
			return Optional.ofNullable(max);
			
		} catch (FeatureCalcException e) {
			throw new CreateException(e);
		}
	}
}
