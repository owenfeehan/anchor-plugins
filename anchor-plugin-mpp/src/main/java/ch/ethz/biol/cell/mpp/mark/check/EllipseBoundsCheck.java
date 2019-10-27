package ch.ethz.biol.cell.mpp.mark.check;

import org.anchoranalysis.core.index.GetOperationFailedException;
import org.anchoranalysis.feature.bean.list.FeatureList;
import org.anchoranalysis.feature.nrg.NRGStackWithParams;

/*
 * #%L
 * anchor-plugin-mpp
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


import ch.ethz.biol.cell.core.CheckMark;
import ch.ethz.biol.cell.mpp.mark.Mark;
import ch.ethz.biol.cell.mpp.mark.MarkEllipse;
import ch.ethz.biol.cell.mpp.mark.regionmap.RegionMap;

public class EllipseBoundsCheck extends CheckMark {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7367208466737073130L;
	
	// START BEAN PROPERTIES
	// END BEAN PROPERTIES

	@Override
	public boolean check(Mark mark, RegionMap regionMap, NRGStackWithParams nrgStack) throws CheckException {
		
		MarkEllipse me = (MarkEllipse) mark;
		
		try {
			double minBound = getSharedObjects().getMarkBounds().getMinRslvd(nrgStack.getDimensions().getRes(), false);
			double maxBound = getSharedObjects().getMarkBounds().getMaxRslvd(nrgStack.getDimensions().getRes(), false);
			
			if (me.getRadii().getX() < minBound) {
				return false;
			}
			
			if (me.getRadii().getY() < minBound) {
				return false;
			}
			
			if (me.getRadii().getX() > maxBound) {
				return false;
			}
			
			if (me.getRadii().getY() > maxBound) {
				return false;
			}
			
			return true;
			
		} catch (GetOperationFailedException e) {
			throw new CheckException("Cannot establish bounds", e);
		}
	}

	@Override
	public boolean isCompatibleWith(Mark testMark) {
		return testMark instanceof MarkEllipse;
	}

	@Override
	public FeatureList orderedListOfFeatures() {
		return null;
	}

}
