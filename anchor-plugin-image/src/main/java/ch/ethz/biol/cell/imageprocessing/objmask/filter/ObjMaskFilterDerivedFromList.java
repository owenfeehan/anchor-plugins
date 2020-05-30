package ch.ethz.biol.cell.imageprocessing.objmask.filter;

import java.util.ArrayList;
import java.util.List;

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
import org.anchoranalysis.image.bean.objmask.filter.ObjMaskFilter;


/**
 * A filter whose behaviour derives from a list of other filters.
 * 
 * @author Owen Feehan
 *
 */
public abstract class ObjMaskFilterDerivedFromList extends ObjMaskFilter {

	// START BEAN PROPERTIES
	/** A list of other filters from which the sub-classes implement filtering behaviour. */
	@BeanField
	private List<ObjMaskFilter> list = new ArrayList<>();
	// END BEAN PROPERTIES

	public List<ObjMaskFilter> getList() {
		return list;
	}

	public void setList(List<ObjMaskFilter> list) {
		this.list = list;
	}
	
	

}
