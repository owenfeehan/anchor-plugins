package ch.ethz.biol.cell.imageprocessing.objmask.filter;

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


import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.image.bean.provider.ObjMaskProvider;
import org.anchoranalysis.image.extent.ImageDim;
import org.anchoranalysis.image.objmask.ObjMask;
import org.anchoranalysis.image.objmask.ObjMaskCollection;

// Only accepts an object if it has greater (or EQUAL) intersection with objMaskProviderGreater than objMaskProviderLesser
// So if an object intersects with neither, it still gets accepted, as both return 0
public class ObjMaskFilterGreaterIntersectionWith extends ObjMaskFilterByObject {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	// START BEAN PROPERTIES
	@BeanField
	private ObjMaskProvider objMaskProviderGreater;
	
	@BeanField
	private ObjMaskProvider objMaskProviderLesser;
	// END BEAN PROPERTIES
	
	private ObjMaskCollection intersectionSetGreater;
	private ObjMaskCollection intersectionSetLesser;

	@Override
	protected void start(ImageDim dim) throws OperationFailedException {
		
		try {
			intersectionSetGreater = objMaskProviderGreater.create();
			intersectionSetLesser = objMaskProviderLesser.create();
			
		} catch (CreateException e) {
			throw new OperationFailedException(e);
		}
		
	}


	
	@Override
	protected boolean match(ObjMask om, ImageDim dim)
			throws OperationFailedException {

		int cntGreater =  intersectionSetGreater.countIntersectingPixels(om );
		int cntLesser =  intersectionSetLesser.countIntersectingPixels(om );
		
		return cntGreater >= cntLesser;
	}

	@Override
	protected void end() throws OperationFailedException {
		intersectionSetGreater = null;
		intersectionSetLesser = null;
	}


	public ObjMaskProvider getObjMaskProviderGreater() {
		return objMaskProviderGreater;
	}

	public void setObjMaskProviderGreater(ObjMaskProvider objMaskProviderGreater) {
		this.objMaskProviderGreater = objMaskProviderGreater;
	}

	public ObjMaskProvider getObjMaskProviderLesser() {
		return objMaskProviderLesser;
	}

	public void setObjMaskProviderLesser(ObjMaskProvider objMaskProviderLesser) {
		this.objMaskProviderLesser = objMaskProviderLesser;
	}



}
