package ch.ethz.biol.cell.imageprocessing.chnl.provider;

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


import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.error.CreateException;


/**
 * Extracts the gradient
 * 
 * @author Owen Feehan
 *
 */
public class ChnlProviderGradientSingleDimension extends ChnlProviderGradientBase {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3978226156945187112L;
	
	// START BEAN
	/** Which axis? X=0, Y=1, Z=2 */ 
	@BeanField
	private int axis = 0;
	// END BEAN

	@Override
	protected boolean[] createDimensionArr() throws CreateException {
		switch(axis) {
		case 0:
			return new boolean[] { true, false, false };
		case 1:
			return new boolean[] { false, true, false };
		case 2:
			return new boolean[] { false, false, true };
		default:
			throw new CreateException("Axis must be 0 (x-axis) or 1 (y-axis) or 2 (z-axis)");
		}
	}

	public int getAxis() {
		return axis;
	}

	public void setAxis(int axis) {
		this.axis = axis;
	}
}
