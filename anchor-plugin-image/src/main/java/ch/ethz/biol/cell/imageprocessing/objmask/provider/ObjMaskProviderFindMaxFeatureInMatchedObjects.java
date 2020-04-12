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


import java.util.List;

import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.feature.session.calculator.FeatureCalculatorSingle;
import org.anchoranalysis.image.bean.objmask.match.ObjMaskMatcher;
import org.anchoranalysis.image.bean.provider.ObjMaskProvider;
import org.anchoranalysis.image.feature.bean.evaluator.FeatureEvaluator;
import org.anchoranalysis.image.feature.objmask.FeatureObjMaskParams;
import org.anchoranalysis.image.objmask.ObjMask;
import org.anchoranalysis.image.objmask.ObjMaskCollection;
import org.anchoranalysis.image.objmask.match.ObjWithMatches;

// Returns a collection of each Max Object found in matches
public class ObjMaskProviderFindMaxFeatureInMatchedObjects extends ObjMaskProvider {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	// START BEAN PROPERTIES
	@BeanField
	private ObjMaskProvider objs;
	
	@BeanField
	private ObjMaskMatcher objMaskMatcher;
	
	@BeanField
	private FeatureEvaluator<FeatureObjMaskParams> featureEvaluator;
	// END BEAN PROPERTIES

	@Override
	public ObjMaskCollection create() throws CreateException {
		
		ObjMaskCollection in = objs.create();
		
		FeatureCalculatorSingle<FeatureObjMaskParams> session;
		try {
			session = featureEvaluator.createAndStartSession();
		} catch (OperationFailedException e) {
			throw new CreateException(e);
		}


		ObjMaskCollection out = new ObjMaskCollection();
		try {
			List<ObjWithMatches> listMatches = objMaskMatcher.findMatch(in);
	
			for( ObjWithMatches owm : listMatches ) {
			
				ObjMask max = findMax( session, owm.getMatches() );
				if (max!=null) {
					out.add(max);
				}
			}
		} catch (OperationFailedException e) {
			throw new CreateException(e);
		} catch (FeatureCalcException e) {
			throw new CreateException(e);
		}
		
		return out;
	}
	
	private ObjMask findMax( FeatureCalculatorSingle<FeatureObjMaskParams> session, ObjMaskCollection objs ) throws FeatureCalcException {
		ObjMask max = null;
		double maxVal = 0;
		
		for( ObjMask om : objs ) {
			
			double featureVal = session.calcOne(
				new FeatureObjMaskParams(om)
			);
			
			if (max==null || featureVal>maxVal) {
				max = om;
				maxVal = featureVal;
			}
		}
		
		return max;
	}
	
	public ObjMaskProvider getObjs() {
		return objs;
	}

	public void setObjs(ObjMaskProvider objs) {
		this.objs = objs;
	}

	public FeatureEvaluator<FeatureObjMaskParams> getFeatureEvaluator() {
		return featureEvaluator;
	}

	public void setFeatureEvaluator(FeatureEvaluator<FeatureObjMaskParams> featureEvaluator) {
		this.featureEvaluator = featureEvaluator;
	}

	public ObjMaskMatcher getObjMaskMatcher() {
		return objMaskMatcher;
	}

	public void setObjMaskMatcher(ObjMaskMatcher objMaskMatcher) {
		this.objMaskMatcher = objMaskMatcher;
	}
}
