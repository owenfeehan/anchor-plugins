package ch.ethz.biol.cell.imageprocessing.objmask.filter;

import java.util.Optional;

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
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.core.geometry.Point3i;
import org.anchoranalysis.image.extent.ImageDim;
import org.anchoranalysis.image.objmask.ObjMask;

public class ObjMaskFilterRemoveTouchingSceneBorder extends ObjMaskFilterByObject {

	// START BEAN PROPERTIES
	@BeanField
	private boolean includeZ = false;
	// END BEAN PROPERTIES
	
	@Override
	protected void start() throws OperationFailedException {

	}

	@Override
	protected boolean match(ObjMask om, Optional<ImageDim> dim)
			throws OperationFailedException {
		
		if (!dim.isPresent()) {
			throw new OperationFailedException("Image-dimensions are required for this operation");
		}
		
		if (om.getBoundingBox().atBorderXY(dim.get())) {
			return false;
		}
		
		if (includeZ) {
			Point3i crnrMin = om.getBoundingBox().getCrnrMin();
			if (crnrMin.getZ()==0) {
				return false;
			}

			Point3i crnrMax = om.getBoundingBox().calcCrnrMax();
			if (crnrMax.getZ()==(dim.get().getZ()-1)) {
				return false;
			}
		}
		return true;
	}

	@Override
	protected void end() throws OperationFailedException {
	}

	public boolean isIncludeZ() {
		return includeZ;
	}

	public void setIncludeZ(boolean includeZ) {
		this.includeZ = includeZ;
	}

}
