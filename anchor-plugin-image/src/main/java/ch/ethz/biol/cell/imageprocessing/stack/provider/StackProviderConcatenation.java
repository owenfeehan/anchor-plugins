package ch.ethz.biol.cell.imageprocessing.stack.provider;

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
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.image.bean.provider.BeanImgStackProvider;
import org.anchoranalysis.image.bean.provider.stack.StackProvider;
import org.anchoranalysis.image.extent.IncorrectImageSizeException;
import org.anchoranalysis.image.stack.Stack;

public class StackProviderConcatenation extends StackProvider {

	// START BEAN PROPERTIES
	@BeanField
	private List<BeanImgStackProvider<?,?>> list = new ArrayList<>();
	// END BEAN PROPERTIES
		
	@Override
	public Stack create() throws CreateException {
		
		try {
			Stack out = new Stack();
			
			for (BeanImgStackProvider<?,?> provider : list) {
				
				Stack crnt = provider.createStack();
				
				for( int c=0;  c<crnt.getNumChnl(); c++ ) {
					out.addChnl( crnt.getChnl(c) );
				}
			
			}
				
			return out;
			
		} catch (IncorrectImageSizeException e) {
			throw new CreateException(e);
		}
	}

	public List<BeanImgStackProvider<?,?>> getList() {
		return list;
	}

	public void setList(List<BeanImgStackProvider<?,?>> list) {
		this.list = list;
	}


}
