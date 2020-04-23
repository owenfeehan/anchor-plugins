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
import org.anchoranalysis.feature.bean.Feature;
import org.anchoranalysis.feature.bean.operator.FeatureGenericSingleElem;
import org.anchoranalysis.feature.cache.SessionInput;
import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.feature.calc.params.FeatureInput;

public class IfGreaterThan<T extends FeatureInput> extends FeatureGenericSingleElem<T> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	// START BEAN PROPERTIRES
	@BeanField
	private Feature<T> featureCondition;
	
	@BeanField
	private double value;
	
	@BeanField
	private Feature<T> featureElse;
	// END BEAN PROPERTIES

	@Override
	public double calc(SessionInput<T> input)
			throws FeatureCalcException {

		if (input.calc(featureCondition)>value) {
			return input.calc(super.getItem());
		} else {
			return input.calc(featureElse);
		}
	}
	
	public Feature<T> getFeatureCondition() {
		return featureCondition;
	}
	public void setFeatureCondition(Feature<T> featureCondition) {
		this.featureCondition = featureCondition;
	}
	public double getValue() {
		return value;
	}
	public void setValue(double value) {
		this.value = value;
	}
	public Feature<T> getFeatureElse() {
		return featureElse;
	}
	public void setFeatureElse(Feature<T> featureElse) {
		this.featureElse = featureElse;
	}

	@Override
	public String getParamDscr() {
		return String.format("value=%e",value);
	}


}
