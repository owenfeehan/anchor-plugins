package ch.ethz.biol.cell.mpp.mark.ellipsoidfitter.outlinepixelsretriever.visitscheduler;

/*-
 * #%L
 * anchor-plugin-mpp
 * %%
 * Copyright (C) 2010 - 2019 Owen Feehan, ETH Zurich, University of Zurich, Hoffmann la Roche
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
import org.anchoranalysis.core.error.InitException;
import org.anchoranalysis.core.geometry.Point3i;
import org.anchoranalysis.core.geometry.Tuple3i;
import org.anchoranalysis.core.random.RandomNumberGenerator;
import org.anchoranalysis.image.bean.unitvalue.distance.UnitValueDistance;
import org.anchoranalysis.image.extent.ImageRes;
import org.anchoranalysis.image.objmask.ObjMask;
import org.anchoranalysis.image.orientation.DirectionVector;

// Breadth-first iteration of pixels
public class VisitSchedulerMaxDistUnitValue extends VisitScheduler {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	// START BEAN PROPERTIES
	@BeanField
	private UnitValueDistance maxDist;
	// END BEAN PROPERTIES
	
	private Point3i root;
	
	private ImageRes res;
	
	
	
	public VisitSchedulerMaxDistUnitValue() {
		super();
	}

	
	@Override
	public void beforeCreateObjMask(RandomNumberGenerator re, ImageRes res)
			throws InitException {
			
	}
	
	@Override
	public Tuple3i maxDistFromRootPoint(ImageRes res) {
		int distX = (int) Math.ceil( maxDist.rslv(res, new DirectionVector(1,0,0)) );
		int distY = (int) Math.ceil( maxDist.rslv(res, new DirectionVector(0,1,0)) );
		int distZ = (int) Math.ceil( maxDist.rslv(res, new DirectionVector(0,0,1)) );
		return new Point3i(distX,distY,distZ);
	}
	
	@Override
	public void afterCreateObjMask(Point3i root, ImageRes res, RandomNumberGenerator re) {
		this.res = res;
		this.root = root;
	}

	@Override
	public boolean considerVisit( Point3i pnt, int distAlongContour, ObjMask objMask ) {
		
		if (distToRoot(pnt)>=maxDist.rslv(res, root, pnt )) {
			return false;
		}
		
		return true;
	}
	
	private double distToRoot( Point3i pnt ) {
		 return res.distance(root, pnt);
	}

	public UnitValueDistance getMaxDist() {
		return maxDist;
	}

	public void setMaxDist(UnitValueDistance maxDist) {
		this.maxDist = maxDist;
	}

}
