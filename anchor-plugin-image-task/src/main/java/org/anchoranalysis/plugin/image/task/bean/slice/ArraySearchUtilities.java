package org.anchoranalysis.plugin.image.task.bean.slice;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

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

@NoArgsConstructor(access=AccessLevel.PRIVATE)
class ArraySearchUtilities {

	public static int findIndexOfMaximum( double[] arr ) {
		double max = Double.MIN_VALUE;
		int maxIndex = -1;
		for (int i=0; i<arr.length; i++) {
			double val = arr[i];
			if (val>max) {
				maxIndex = i;
				max = val;
			}
		}
		return maxIndex;
	}
	
	public static int findIndexOfMinimum( double[] arr ) {
		double min = Double.MAX_VALUE;
		int minIndex = -1;
		for (int i=0; i<arr.length; i++) {
			double val = arr[i];
			if (val<min) {
				minIndex = i;
				min = val;
			}
		}
		return minIndex;
	}
}
