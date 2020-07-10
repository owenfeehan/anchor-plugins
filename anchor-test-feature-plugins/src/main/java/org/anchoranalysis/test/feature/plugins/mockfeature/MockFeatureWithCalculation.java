package org.anchoranalysis.test.feature.plugins.mockfeature;

/*-
 * #%L
 * anchor-test-feature-plugins
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

import org.anchoranalysis.feature.bean.Feature;
import org.anchoranalysis.feature.cache.SessionInput;
import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.feature.input.FeatureInput;

/**
 * A feature that returns the number-of-voxels in an object by using am internal mock-calculation.
 * <p>
 * Warning! Method calls are counted via ugly static variable implementation.
 * 
 * @author Owen Feehan
 *
 */
public class MockFeatureWithCalculation extends Feature<FeatureInput> {
	
	/** 
	 * Records a count of the number of calls to {@link #calc}
	 * <p>
	 * It is incremented every time {@link #calc} is called.
	 */
	static int cntCalcCalled = 0;
	
	@Override
	protected double calc(SessionInput<FeatureInput> input) throws FeatureCalcException {
		cntCalcCalled++;										// NOSONAR
		MockCalculation calculation = new MockCalculation();
		return input.calc( calculation );	
	}

	@Override
	public Class<? extends FeatureInput> inputType() {
		return FeatureInput.class;
	}
}
