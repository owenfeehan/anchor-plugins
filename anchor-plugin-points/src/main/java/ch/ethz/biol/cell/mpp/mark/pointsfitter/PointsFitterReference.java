package ch.ethz.biol.cell.mpp.mark.pointsfitter;

/*
 * #%L
 * anchor-plugin-points
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
import org.anchoranalysis.core.error.InitException;
import org.anchoranalysis.core.geometry.Point3f;
import org.anchoranalysis.core.index.GetOperationFailedException;
import org.anchoranalysis.image.extent.ImageDim;

import ch.ethz.biol.cell.beaninitparams.PointsInitParams;
import ch.ethz.biol.cell.mpp.mark.Mark;

public class PointsFitterReference extends PointsFitter {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5462890873512434544L;

	// START BEAN
	@BeanField
	private String id;
	// END BEAN

	private PointsFitter pointsFitter;

	@Override
	public void onInit(PointsInitParams so)
			throws InitException {
		super.onInit(so);
		try {
			this.pointsFitter = getSharedObjects().getPointsFitterSet().getException(id);
		} catch (GetOperationFailedException e) {
			throw new InitException(e);
		}
	}

	@Override
	public boolean isCompatibleWith(Mark testMark) {
		return pointsFitter.isCompatibleWith(testMark);
	}

	@Override
	public void fit(List<Point3f> points, Mark mark, ImageDim dim)
			throws PointsFitterException, InsufficientPointsException {
		pointsFitter.fit(points, mark, dim);
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}


}
