package org.anchoranalysis.plugin.image.bean.histogram.threshold;

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

import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.image.bean.threshold.CalculateLevel;
import org.anchoranalysis.image.bean.threshold.CalculateLevelOne;
import org.anchoranalysis.image.histogram.Histogram;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@EqualsAndHashCode(callSuper = false)
public abstract class CalculateLevelRelativeToThreshold extends CalculateLevelOne {

	// START BEAN PROPERTIES
	@BeanField @Getter @Setter
	private CalculateLevel calculateLevelElse;
	
	@BeanField @Getter @Setter
	private int threshold;
	// END BEAN PROPERTIES
	
	@Override
	public int calculateLevel(Histogram h) throws OperationFailedException {

		int level = calculateLevelIncoming(h);
		if (useElseInstead(level,threshold)) {
			return calculateLevelElse.calculateLevel(h);
		} else {
			return level;
		}
	}
	
	/** Uses the {@link calculateLevelElse} instead of {@link calculateLevel} */
	protected abstract boolean useElseInstead(int level, int threshold);
}
