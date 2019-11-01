package ch.ethz.biol.cell.mpp.nrg.cachedcalculation;

import org.anchoranalysis.anchor.mpp.overlap.OverlapUtilities;
import org.anchoranalysis.anchor.mpp.pxlmark.memo.PxlMarkMemo;

/*
 * #%L
 * anchor-plugin-mpp-feature
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


import org.anchoranalysis.core.cache.ExecuteException;
import org.anchoranalysis.feature.cachedcalculation.CachedCalculation;
import org.anchoranalysis.feature.cachedcalculation.CachedCalculationCastParams;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import ch.ethz.biol.cell.mpp.nrg.NRGElemPairCalcParams;

public class OverlapCalculation extends CachedCalculationCastParams<Double,NRGElemPairCalcParams> {

	private int regionID;
	
	// Constructor
	public OverlapCalculation( int regionID ) {
		super();
		this.regionID = regionID;
	}

	@Override
	protected Double execute( NRGElemPairCalcParams params ) throws ExecuteException {
		PxlMarkMemo mark1 = params.getObj1();
		PxlMarkMemo mark2 = params.getObj2();
		
		assert( mark1 != null );
		assert( mark2 != null );
		
		return OverlapUtilities.overlapWith(mark1,mark2,regionID);
	}

	@Override
	public CachedCalculation<Double> duplicate() {
		return new OverlapCalculation( regionID );
	}
	
	@Override
	public int hashCode() {
		return new HashCodeBuilder().append(regionID).toHashCode();
	}
	
	@Override
	public boolean equals(final Object obj){
	    if(obj instanceof OverlapCalculation){
	        final OverlapCalculation other = (OverlapCalculation) obj;
	        return new EqualsBuilder()
	            .append(regionID, other.regionID)
	            .isEquals();
	    } else{
	        return false;
	    }
	}
}
