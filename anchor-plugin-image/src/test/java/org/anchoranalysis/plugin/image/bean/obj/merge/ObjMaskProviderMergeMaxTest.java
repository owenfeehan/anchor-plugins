package org.anchoranalysis.plugin.image.bean.obj.merge;

/*-
 * #%L
 * anchor-plugin-image
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

import java.util.function.Function;

import org.anchoranalysis.bean.error.BeanMisconfiguredException;
import org.anchoranalysis.bean.xml.RegisterBeanFactories;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.error.InitException;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.core.log.LogErrorReporter;
import org.anchoranalysis.image.feature.object.input.FeatureInputSingleObject;
import org.anchoranalysis.image.objectmask.ObjectCollection;
import org.anchoranalysis.plugin.image.test.ProviderFixture;
import org.anchoranalysis.test.LoggingFixture;
import org.anchoranalysis.test.feature.plugins.mockfeature.MockFeatureWithCalculationFixture;
import org.junit.Test;

public class ObjMaskProviderMergeMaxTest {
	
	static {
		RegisterBeanFactories.registerAllPackageBeanFactories();
	}

	/** This test will merge so long as the total number of pixels increases (so every possible neighbor will eventually merge!) 
	 * @throws OperationFailedException 
	 * @throws CreateException */
	@Test
	public void testMaximalNumPixels() throws BeanMisconfiguredException, InitException, OperationFailedException, CreateException {
		testLinear(
			MergeTestHelper.EXPECTED_RESULT_ALL_INTERSECTING_MERGED,
			24,
			MockFeatureWithCalculationFixture.DEFAULT_FUNC_NUM_PIXELS
		);
	}
	
	/** 
	 * This tests will merge if it brings the total number of pixels closer to 500, but not merge otherwise.
	 * 
	 * <p>Therefore some neighboring objects will merge and others will not</p>
	 * @throws OperationFailedException 
	 * @throws CreateException 
	 */
	@Test
	public void testConvergeNumPixels() throws BeanMisconfiguredException, InitException, OperationFailedException, CreateException {
		testLinear(
			8,
			23,
			ObjMaskProviderMergeMaxTest::convergeTo900
		);
	}
	
	private void testLinear( int expectedCntMerged, int expectedFeatureCalcCount, Function<FeatureInputSingleObject,Double> calculationFunc ) throws OperationFailedException, CreateException {
		MergeTestHelper.testProviderOn(
			expectedCntMerged,
			expectedFeatureCalcCount,
			expectedFeatureCalcCount,	// Same as the feature-count as it should always be a new unique object
			createMergeMax(MergeTestHelper.OBJS_LINEAR_INTERSECTING, calculationFunc)
		);
	}
	
	private static ObjMaskProviderMergeMax createMergeMax( ObjectCollection objs, Function<FeatureInputSingleObject,Double> calculationFunc ) throws CreateException {
		
		LogErrorReporter logger = LoggingFixture.suppressedLogErrorReporter();
		
		ObjMaskProviderMergeMax provider = new ObjMaskProviderMergeMax();
		
		provider.setObjs(
			ProviderFixture.providerFor(objs)
		);
		provider.setFeatureEvaluator(
			FeatureEvaluatorFixture.createNrg(
				MockFeatureWithCalculationFixture.createMockFeatureWithCalculation(calculationFunc),
				logger
			)
		);
		
		return provider;
	}
	
	/** Merges if the number-of-pixels becomes closer to 900 */
	private static Double convergeTo900( FeatureInputSingleObject input ) {
		int diff = 900 - input.getObjMask().numVoxelsOn();
		return (double) -1 * Math.abs(diff);
	}
}
