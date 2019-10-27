package ch.ethz.biol.cell.imageprocessing.objmask.provider;

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


import java.util.List;

import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.annotation.Optional;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.image.bean.objmask.filter.ObjMaskFilter;
import org.anchoranalysis.image.bean.provider.ObjMaskProvider;
import org.anchoranalysis.image.extent.ImageDim;
import org.anchoranalysis.image.objmask.ObjMaskCollection;
import org.anchoranalysis.image.objmask.match.ObjWithMatches;

import ch.ethz.biol.cell.imageprocessing.objmask.matching.ObjMaskMatchUtilities;

// TODO lots of duplication with ObjMaskProviderFilter
public class ObjMaskProviderFilterByGroup extends ObjMaskProviderDimensionsOptional {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	// START BEAN PROPERTIES
	@BeanField
	private ObjMaskFilter objMaskFilter;
	
	@BeanField
	private ObjMaskProvider objMaskProvider;
	
	@BeanField
	private ObjMaskProvider objMaskProviderGrouped;
	
	@BeanField @Optional
	private ObjMaskProvider objMaskProviderRejected;	// The rejected objects are put here (OPTIONAL)
	// END BEAN PROPERTIES
	
	@Override
	public ObjMaskCollection create() throws CreateException {
		
		ObjMaskCollection in = objMaskProvider.create();
		
		ObjMaskCollection inGroups = objMaskProviderGrouped.create();
		
		ObjMaskCollection omcRejected = objMaskProviderRejected!=null ? objMaskProviderRejected.create() : null;
		
		List<ObjWithMatches> matchList = ObjMaskMatchUtilities.matchIntersectingObjects( inGroups, in );
		
		ImageDim dims = createDims();
				
		ObjMaskCollection out = new ObjMaskCollection();
		
		try {
			for( ObjWithMatches wm : matchList ) {
				ObjMaskCollection objs = wm.getMatches();
				objMaskFilter.filter(objs, dims, omcRejected);
				out.addAll( objs );
			}
		} catch (OperationFailedException e) {
			throw new CreateException(e);
		}
		return out;
	}

	public ObjMaskFilter getObjMaskFilter() {
		return objMaskFilter;
	}

	public void setObjMaskFilter(ObjMaskFilter objMaskFilter) {
		this.objMaskFilter = objMaskFilter;
	}

	public ObjMaskProvider getObjMaskProvider() {
		return objMaskProvider;
	}

	public void setObjMaskProvider(ObjMaskProvider objMaskProvider) {
		this.objMaskProvider = objMaskProvider;
	}

	public ObjMaskProvider getObjMaskProviderRejected() {
		return objMaskProviderRejected;
	}

	public void setObjMaskProviderRejected(ObjMaskProvider objMaskProviderRejected) {
		this.objMaskProviderRejected = objMaskProviderRejected;
	}

	public ObjMaskProvider getObjMaskProviderGrouped() {
		return objMaskProviderGrouped;
	}

	public void setObjMaskProviderGrouped(ObjMaskProvider objMaskProviderGrouped) {
		this.objMaskProviderGrouped = objMaskProviderGrouped;
	}
}
