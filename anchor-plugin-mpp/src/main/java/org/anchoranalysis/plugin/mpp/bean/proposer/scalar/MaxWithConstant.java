package org.anchoranalysis.plugin.mpp.bean.proposer.scalar;

import org.anchoranalysis.anchor.mpp.bean.proposer.ScalarProposer;

/*
 * #%L
 * anchor-plugin-mpp
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
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.core.random.RandomNumberGenerator;
import org.anchoranalysis.image.extent.ImageResolution;

public class MaxWithConstant extends ScalarProposer {

	// START BEAN PROPERTIES
	@BeanField
	private ScalarProposer scalarProposer;
	
	@BeanField
	private double constant = 1.0;
	// END BEAN PROPERTIES

	@Override
	public double propose(RandomNumberGenerator re, ImageResolution res)
			throws OperationFailedException {

		double value = scalarProposer.propose(re, res); 
		return Math.max(value,constant);
	}

	public ScalarProposer getScalarProposer() {
		return scalarProposer;
	}

	public void setScalarProposer(ScalarProposer scalarProposer) {
		this.scalarProposer = scalarProposer;
	}

	public double getConstant() {
		return constant;
	}

	public void setConstant(double constant) {
		this.constant = constant;
	}

}
