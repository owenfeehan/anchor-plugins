package org.anchoranalysis.plugin.image.bean.threshold.calculatelevel;

/*
 * #%L
 * anchor-plugin-image
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


import java.util.ArrayList;
import java.util.List;

import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.image.bean.threshold.CalculateLevel;
import org.anchoranalysis.image.histogram.Histogram;
import org.apache.commons.lang.builder.HashCodeBuilder;


/**
 * The maximum value of one or more {#link org.anchoranalysis.image.bean.threshold.CalculateLevel}
 * 
 * @author owen
 *
 */
public class Maximum extends CalculateLevel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	// START BEAN PROPERTIES
	@BeanField
	private List<CalculateLevel> list = new ArrayList<>();
	// END BEAN PROPERTIES
	
	@Override
	public int calculateLevel(Histogram h) throws OperationFailedException {

		int max = -1;	// As level should always be >=0
		for( CalculateLevel cl : list) {
			int level = cl.calculateLevel(h);
			if (level>max) {
				max = level;
			}
		}
		return max;
	}

	public List<CalculateLevel> getList() {
		return list;
	}

	public void setList(List<CalculateLevel> list) {
		this.list = list;
	}
	
	@Override
	public boolean equals(Object obj) {
		assert(false);
		return false;
	}

	@Override
	public int hashCode() {
		assert(false);
		return new HashCodeBuilder()
			.toHashCode();
	}
}
