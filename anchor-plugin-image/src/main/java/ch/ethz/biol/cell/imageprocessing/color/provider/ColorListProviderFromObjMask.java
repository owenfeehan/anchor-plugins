package ch.ethz.biol.cell.imageprocessing.color.provider;

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
import org.anchoranalysis.core.color.ColorList;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.image.bean.provider.ColorListProvider;
import org.anchoranalysis.image.bean.provider.ObjMaskProvider;
import org.anchoranalysis.image.objectmask.ObjectCollection;
import org.anchoranalysis.io.bean.color.generator.ColorSetGenerator;

public class ColorListProviderFromObjMask extends ColorListProvider {

	// START BEAN PROPERTIES
	@BeanField
	private ColorSetGenerator colorSetGenerator;
	
	@BeanField
	private ObjMaskProvider objs;
	// END BEAN PROPERTIES

	@Override
	public ColorList create() throws CreateException {

		ObjectCollection objsCollection;
		try {
			objsCollection = objs.create();
		} catch (CreateException e) {
			throw new CreateException(e);
		}
		
		try {
			return colorSetGenerator.genColors(objsCollection.size());
		} catch (OperationFailedException e) {
			throw new CreateException(e);
		}
	}

	public ColorSetGenerator getColorSetGenerator() {
		return colorSetGenerator;
	}


	public void setColorSetGenerator(ColorSetGenerator colorSetGenerator) {
		this.colorSetGenerator = colorSetGenerator;
	}


	public ObjMaskProvider getObjs() {
		return objs;
	}


	public void setObjs(ObjMaskProvider objs) {
		this.objs = objs;
	}


}
