package org.anchoranalysis.plugin.image.task.bean.labeller;

/*-
 * #%L
 * anchor-plugin-image-task
 * %%
 * Copyright (C) 2010 - 2019 Owen Feehan, ETH Zurich, University of Zurich, Hoffmann la Roche
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
import java.util.List;
import java.util.Optional;

import org.anchoranalysis.bean.NamedBean;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.annotation.NonEmpty;
import org.anchoranalysis.bean.annotation.SkipInit;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.experiment.task.NoSharedState;
import org.anchoranalysis.feature.bean.list.FeatureListProvider;
import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.image.bean.provider.stack.StackProvider;
import org.anchoranalysis.image.feature.stack.FeatureInputStack;
import org.anchoranalysis.image.io.input.ProvidesStackInput;
import org.anchoranalysis.io.output.bound.BoundIOContext;
import org.anchoranalysis.plugin.image.task.imagefeature.calculator.FeatureCalculatorFromProviderFactory;

public class BinaryClassifierImageLabeller extends BinaryOutcomeImageLabeller {
	
	// START BEAN PROPERTIES
	@BeanField @SkipInit
	private FeatureListProvider<FeatureInputStack> classifierProvider;
	
	@BeanField @NonEmpty
	private List<NamedBean<FeatureListProvider<FeatureInputStack>>> listFeatures = new ArrayList<>();
	
	@BeanField
	private StackProvider nrgStackProvider;
	// END BEAN PROPERTIES
	
	@Override
	public String labelFor(
		NoSharedState sharedState,
		ProvidesStackInput input,
		BoundIOContext context
	) throws OperationFailedException {
		
		try {
			FeatureCalculatorFromProviderFactory<FeatureInputStack> featureCalculator = new FeatureCalculatorFromProviderFactory<>(
				input,
				Optional.of(
					getNrgStackProvider()
				),
				context
			);
			
			double classificationVal = featureCalculator.calculatorSingleFromProvider(
				classifierProvider,
				"classifierProvider"
			).calc( new FeatureInputStack() );
	
			context.getLogReporter().logFormatted("Classification value = %f", classificationVal);
					
			// If classification val is >= 0, then it is POSITIVE
			// If classification val is < 0, then it is NEGATIVE
			boolean classificationPositive = classificationVal>=0;
			
			return classificationString(classificationPositive);
			
		} catch (FeatureCalcException e) {
			throw new OperationFailedException(e);
		}
	}
	
	public FeatureListProvider<FeatureInputStack> getClassifierProvider() {
		return classifierProvider;
	}

	public void setClassifierProvider(FeatureListProvider<FeatureInputStack> classifierProvider) {
		this.classifierProvider = classifierProvider;
	}
	
	public List<NamedBean<FeatureListProvider<FeatureInputStack>>> getListFeatures() {
		return listFeatures;
	}

	public void setListFeatures(
			List<NamedBean<FeatureListProvider<FeatureInputStack>>> listFeatures) {
		this.listFeatures = listFeatures;
	}

	public StackProvider getNrgStackProvider() {
		return nrgStackProvider;
	}

	public void setNrgStackProvider(StackProvider nrgStackProvider) {
		this.nrgStackProvider = nrgStackProvider;
	}

}
