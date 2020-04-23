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
import org.anchoranalysis.feature.cache.CacheableParams;
import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.feature.calc.params.FeatureInput;

public class MinMaxRangeReplaceFromFeatures<T extends FeatureInput> extends FeatureGenericSingleElem<T> {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -8939465493485894918L;
	
	// START BEAN PROPERTIES
	@BeanField
	private double belowMinValue = 0;
	
	@BeanField
	private double aboveMaxValue = 0;
	
	@BeanField
	private double withinValue = 0;
	
	@BeanField
	private Feature<T> min;
	
	@BeanField
	private Feature<T> max;
	// END BEAN PROPERTIES
	
	@Override
	public double calc( CacheableParams<T> params ) throws FeatureCalcException {
		
		double val = params.calc( getItem() );
		double valMin = params.calc(min);
		double valMax = params.calc(max);
		
		if (val < valMin) {
			return belowMinValue;
		}
		
		if (val > valMax) {
			return aboveMaxValue;
		}
		
		return withinValue;
	}

	@Override
	public String getParamDscr() {
		return String.format("min=%s,max=%s,withinValue=%f,belowMinValue=%f,aboveMaxValue=%f", min.getFriendlyName(), max.getFriendlyName(), withinValue, belowMinValue, aboveMaxValue );
	}
	
	public Feature<T> getMin() {
		return min;
	}

	public void setMin(Feature<T> min) {
		this.min = min;
	}

	public Feature<T> getMax() {
		return max;
	}

	public void setMax(Feature<T> max) {
		this.max = max;
	}

	public double getBelowMinValue() {
		return belowMinValue;
	}

	public void setBelowMinValue(double belowMinValue) {
		this.belowMinValue = belowMinValue;
	}

	public double getAboveMaxValue() {
		return aboveMaxValue;
	}

	public void setAboveMaxValue(double aboveMaxValue) {
		this.aboveMaxValue = aboveMaxValue;
	}

	public double getWithinValue() {
		return withinValue;
	}

	public void setWithinValue(double withinValue) {
		this.withinValue = withinValue;
	}
}
