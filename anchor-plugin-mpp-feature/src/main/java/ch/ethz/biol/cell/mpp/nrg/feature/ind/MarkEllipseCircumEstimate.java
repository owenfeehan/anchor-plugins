package ch.ethz.biol.cell.mpp.nrg.feature.ind;

import org.anchoranalysis.anchor.mpp.feature.bean.nrg.elem.NRGElemInd;
import org.anchoranalysis.anchor.mpp.feature.nrg.elem.NRGElemIndCalcParams;
import org.anchoranalysis.anchor.mpp.mark.GlobalRegionIdentifiers;
import org.anchoranalysis.anchor.mpp.mark.Mark;
import org.anchoranalysis.anchor.mpp.mark.conic.MarkEllipse;

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
import org.anchoranalysis.feature.calc.FeatureCalcException;

// Estimates the circumference of an ellipse based upon finding the area of the shell, and dividing
//  by the ShellRad
public class MarkEllipseCircumEstimate extends NRGElemInd {

	
	/**
	 * 
	 */
	private static final long serialVersionUID = 7660902471584365862L;

	// START BEAN PROPERTIES
	@BeanField
	private int regionID = GlobalRegionIdentifiers.SUBMARK_INSIDE;
	// END BEAN PROPERTIES
	
	@Override
	public double calcCast(NRGElemIndCalcParams params) throws FeatureCalcException {
		
		Mark m = params.getPxlPartMemo().getMark();
		
		if (!(m instanceof MarkEllipse)) {
			throw new FeatureCalcException("Only MarkEllipses are supported");
		}
		
		MarkEllipse mark = (MarkEllipse) m;
		
		return mark.circumference(regionID);
	}

	public int getRegionID() {
		return regionID;
	}

	public void setRegionID(int regionID) {
		this.regionID = regionID;
	}
}
