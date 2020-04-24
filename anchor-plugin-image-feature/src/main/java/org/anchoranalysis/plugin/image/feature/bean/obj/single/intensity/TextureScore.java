package org.anchoranalysis.plugin.image.feature.bean.obj.single.intensity;

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
import org.anchoranalysis.feature.cache.SessionInput;
import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.image.chnl.Chnl;
import org.anchoranalysis.image.feature.objmask.FeatureInputSingleObj;
import org.anchoranalysis.image.objmask.ObjMask;


/**
 * From Page 727 from Lin et al (A Multi-Model Approach to Simultaneous Segmentation and Classification of Heterogeneous Populations of Cell Nuclei
 * 
 * @author Owen Feehan
 *
 */
public class TextureScore extends FeatureNrgChnl {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	// START BEAN PROPERTIES
	@BeanField
	private int nrgIndexGradient = 1;
	// END BEAN PROPERTIES

	@Override
	protected double calcForChnl(SessionInput<FeatureInputSingleObj> input, Chnl chnl) throws FeatureCalcException {

		ObjMask om = input.get().getObjMask();
		Chnl chnlGradient = input.get().getNrgStack().getNrgStack().getChnl(nrgIndexGradient);
		
		return scoreFromMeans(
			IntensityMeanCalculator.calcMeanIntensityObjMask(chnl, om),
			IntensityMeanCalculator.calcMeanIntensityObjMask(chnlGradient, om)
		);
	}
	
	private static double scoreFromMeans(double meanIntensity, double meanGradientIntensity) {
		double scaleFactor = 128 / meanIntensity;
		
		return (scaleFactor*meanGradientIntensity)/meanIntensity;
	}

	public int getNrgIndexGradient() {
		return nrgIndexGradient;
	}

	public void setNrgIndexGradient(int nrgIndexGradient) {
		this.nrgIndexGradient = nrgIndexGradient;
	}
}
