package org.anchoranalysis.plugin.image.task.bean.feature;



import java.util.Optional;

/*
 * #%L
 * anchor-plugin-image-task
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
import org.anchoranalysis.bean.annotation.OptionalBean;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.experiment.task.InputTypesExpected;
import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.feature.calc.results.ResultsVector;
import org.anchoranalysis.feature.list.NamedFeatureStore;
import org.anchoranalysis.image.bean.provider.stack.StackProvider;
import org.anchoranalysis.image.feature.stack.FeatureInputStack;
import org.anchoranalysis.image.io.input.ProvidesStackInput;
import org.anchoranalysis.io.output.bound.BoundIOContext;
import org.anchoranalysis.plugin.image.task.imagefeature.calculator.FeatureCalculatorStackInputFromStore;


/** Calculates a feature on each image **/
public class ExportFeaturesImageTask extends ExportFeaturesStoreTask<ProvidesStackInput,FeatureInputStack> {

	// START BEAN PROPERTIES
	@BeanField @OptionalBean
	private StackProvider nrgStackProvider;
	// END BEAN PROPERTIES
	
	public ExportFeaturesImageTask() {
		super("image");
	}
	
	@Override
	public InputTypesExpected inputTypesExpected() {
		return new InputTypesExpected(ProvidesStackInput.class);
	}
	
	@Override
	protected boolean includeGroupInExperiment(boolean groupGeneratorDefined) {
		return groupGeneratorDefined;
	}
	
	@Override
	protected ResultsVector calcResultsVectorForInputObject(
		ProvidesStackInput inputObject,
		NamedFeatureStore<FeatureInputStack> featureStore,
		BoundIOContext context
	) throws FeatureCalcException {

		try {
			FeatureCalculatorStackInputFromStore featCalc = new FeatureCalculatorStackInputFromStore(
				inputObject,
				Optional.ofNullable(
					getNrgStackProvider()
				),
				featureStore,
				context
			);
			
			return featCalc.calcAllInStore();
			
		} catch (OperationFailedException e) {
			throw new FeatureCalcException(e);
		}
	}
	
	public StackProvider getNrgStackProvider() {
		return nrgStackProvider;
	}

	public void setNrgStackProvider(StackProvider nrgStackProvider) {
		this.nrgStackProvider = nrgStackProvider;
	}
}
