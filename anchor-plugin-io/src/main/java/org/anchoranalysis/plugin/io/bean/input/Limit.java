package org.anchoranalysis.plugin.io.bean.input;

/*
 * #%L
 * anchor-io
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


import java.util.List;
import java.util.ListIterator;

import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.log.LogErrorReporter;
import org.anchoranalysis.core.progress.ProgressReporter;
import org.anchoranalysis.io.bean.input.InputManager;
import org.anchoranalysis.io.error.AnchorIOException;
import org.anchoranalysis.io.input.InputFromManager;
import org.anchoranalysis.io.params.InputContextParams;

/**
 * Limits the number of input-objects to a certain hard-maximum
 * 
 * If there's more input-objects than the maximum, the first maxNumItems are
 *   chosen 
 * 
 * @author Owen Feehan
 *
 * @param <T>
 */
public class Limit<T extends InputFromManager> extends InputManager<T> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	// START BEAN PROPERTIES
	@BeanField
	private InputManager<T> input;
	
	@BeanField
	private int maxNumItems = 0;
	// END BEAN PROPERTIES
	
	@Override
	public List<T> inputObjects(InputContextParams inputContext,
			ProgressReporter progressReporter, LogErrorReporter logger) throws AnchorIOException {
		
		int i =0;
		
		List<T> list = input.inputObjects(inputContext, progressReporter, logger);
		
		ListIterator<T> itr = list.listIterator();
		while( itr.hasNext() ) {
			if (i++>=maxNumItems) {
				itr.remove();
			}
		}
		return list;
	}

	public InputManager<T> getInput() {
		return input;
	}

	public void setInput(InputManager<T> input) {
		this.input = input;
	}

	public int getMaxNumItems() {
		return maxNumItems;
	}

	public void setMaxNumItems(int maxNumItems) {
		this.maxNumItems = maxNumItems;
	}

}
