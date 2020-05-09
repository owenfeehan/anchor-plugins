package org.anchoranalysis.plugin.operator.feature.bean.arithmetic;

/*-
 * #%L
 * anchor-plugin-operator-feature
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

import org.anchoranalysis.feature.bean.Feature;
import org.anchoranalysis.feature.bean.operator.FeatureListElem;
import org.anchoranalysis.feature.cache.SessionInput;
import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.feature.input.FeatureInput;


public class Multiply<T extends FeatureInput> extends FeatureListElem<T> {
	
	@Override
	public double calc( SessionInput<T> input ) throws FeatureCalcException {
		
		double result = 1;
		
		for (Feature<T> elem : getList()) {
			result *= input.calc( elem );
			
			// Early exit if we start multiplying by 0
			if (result==0) {
				return result;
			}
		}
		
		return result;
	}

	@Override
	public String getDscrLong() {
		
		StringBuilder sb = new StringBuilder();
		
		boolean first = true;
		for (Feature<T> elem : getList()) {
			
			if (first==true) {
				first = false;
			} else {
				sb.append("*");
			}
		
			sb.append(elem.getDscrLong());
		}
				
		return sb.toString();
	}

}
