package org.anchoranalysis.test.feature.plugins;

/*-
 * #%L
 * anchor-test-feature-plugins
 * %%
 * Copyright (C) 2010 - 2020 Owen Feehan, ETH Zurich, University of Zurich, Hoffmann la Roche
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

import static org.anchoranalysis.test.feature.plugins.ChnlFixture.MEDIUM;

import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.feature.nrg.NRGStack;
import org.anchoranalysis.feature.nrg.NRGStackWithParams;
import org.anchoranalysis.image.extent.ImageDim;
import org.anchoranalysis.image.extent.IncorrectImageSizeException;
import org.anchoranalysis.image.stack.Stack;

public class NRGStackFixture {

	public static NRGStackWithParams create() {
	
		try {
			Stack stack = new Stack();
			stack.addChnl( ChnlFixture.createChnl(MEDIUM, ChnlFixture::sumMod) );
			stack.addChnl( ChnlFixture.createChnl(MEDIUM, ChnlFixture::diffMod) );
			stack.addChnl( ChnlFixture.createChnl(MEDIUM, ChnlFixture::multMod) );
			
			NRGStack nrgStack = new NRGStack(stack);
			return new NRGStackWithParams(nrgStack);
			
		} catch (IncorrectImageSizeException e) {
			assert false;
			return null;
		}
	}
}
