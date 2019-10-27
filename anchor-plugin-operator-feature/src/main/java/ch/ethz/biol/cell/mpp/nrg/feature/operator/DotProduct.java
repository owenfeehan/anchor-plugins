package ch.ethz.biol.cell.mpp.nrg.feature.operator;

/*
 * #%L
 * anchor-feature
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
import org.anchoranalysis.core.geometry.Vector3d;
import org.anchoranalysis.feature.bean.Feature;
import org.anchoranalysis.feature.bean.list.FeatureList;
import org.anchoranalysis.feature.bean.operator.VectorFromFeature;
import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.feature.calc.params.FeatureCalcParams;
import org.anchoranalysis.feature.params.FeatureParamsDescriptor;
import org.anchoranalysis.feature.params.ParamTypeUtilities;

// converts a feature to a physical distance in a XY place that is isometric
public class DotProduct extends Feature {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	// START BEAN PROPERTIES
	@BeanField
	private VectorFromFeature vector1;
	
	@BeanField
	private VectorFromFeature vector2;	
	
	// If TRUE, the opposite direction of the vector is also considered, and the smallest angle is taken
	@BeanField
	private boolean includeOppositeDirection = false;
	// END BEAN PROPERTIES
	
	@Override
	public double calc(FeatureCalcParams params) throws FeatureCalcException {
		
		Vector3d vec1 = vector1.calc(getCacheSession(), params);
		Vector3d vec2 = vector2.calc(getCacheSession(), params);
		
		if (includeOppositeDirection) {
					
			double a = vec1.dot(vec2);
			
			if (a<0) {
				a += 2*Math.PI;
			}
			
			vec2.scale(-1);
			double b = vec1.dot(vec2);

			if (b<0) {
				b += 2*Math.PI;
			}
			
			return Math.min(a, b);
		} else {
			return vec1.dot(vec2);
		}
	}

	public VectorFromFeature getVector1() {
		return vector1;
	}

	public void setVector1(VectorFromFeature vector1) {
		this.vector1 = vector1;
	}

	public VectorFromFeature getVector2() {
		return vector2;
	}

	public void setVector2(VectorFromFeature vector2) {
		this.vector2 = vector2;
	}

	@Override
	public FeatureParamsDescriptor paramType() throws FeatureCalcException {
		return ParamTypeUtilities.paramTypeForTwo(vector1.paramType(), vector2.paramType());
	}

	@Override
	public void addAdditionallyUsedFeatures(FeatureList out) {
		super.addAdditionallyUsedFeatures(out);
		out.add( vector1.getX() );
		out.add( vector1.getY() );
		out.add( vector1.getZ() );
		
		out.add( vector2.getX() );
		out.add( vector2.getY() );
		out.add( vector2.getZ() );
	}

	public boolean isIncludeOppositeDirection() {
		return includeOppositeDirection;
	}

	public void setIncludeOppositeDirection(boolean includeOppositeDirection) {
		this.includeOppositeDirection = includeOppositeDirection;
	}
}
