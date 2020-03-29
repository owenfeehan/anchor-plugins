package ch.ethz.biol.cell.mpp.nrg.feature.ind;

import org.anchoranalysis.anchor.mpp.feature.bean.nrg.elem.NRGElemInd;
import org.anchoranalysis.anchor.mpp.feature.nrg.elem.NRGElemIndCalcParams;

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


import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.shared.relation.EqualToBean;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.relation.RelationToValue;
import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.image.voxel.statistics.VoxelStatistics;

import ch.ethz.biol.cell.mpp.mark.pixelstatisticsfrommark.PixelStatisticsFromMark;

//
// Ratio of number of non-mode pixels to number of pixels
//
public class RatioNonModeValues extends NRGElemInd {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	// START BEAN
	@BeanField
	private PixelStatisticsFromMark pixelList;
	
	@BeanField
	private boolean ignoreZero = false;
	// END BEAN
	
	private static EqualToBean relation = new EqualToBean();
	
	private int findMode( VoxelStatistics stats, int startV ) {
		
		RelationToValue relationToValue = relation.create();
		
		// Find mode
		int maxIndex = -1;
		long maxValue = -1;
		for( int v=startV; v<255; v++) {
			long cnt = stats.countThreshold(relationToValue, v);
			
			if (cnt>maxValue) {
				maxValue = cnt;
				maxIndex = v;
			}
		}
		return maxIndex;
	}
	
	@Override
	public double calcCast( NRGElemIndCalcParams params ) throws FeatureCalcException {

		try {
			VoxelStatistics stats = pixelList.createStatisticsFor(params.getPxlPartMemo(), params.getDimensions() );
			
			int startV = ignoreZero ? 1 : 0;
			
			int mode = findMode(stats, startV);
			
			// Calculate number of non-modal
			int totalCnt = 0;
			int nonModalCnt = 0;

			RelationToValue relationToValue = relation.create();
			
			for( int v=startV; v<255; v++) {
				
				long cnt = stats.countThreshold(relationToValue, v);
				
				if (cnt!=0) {
					if (v!=mode) {
						nonModalCnt += cnt;
					}
					totalCnt += cnt;
				}
			}
			
			if (totalCnt==0) {
				return Double.POSITIVE_INFINITY;
			}
			
			return ((double) nonModalCnt) / totalCnt; 
			
		} catch (IndexOutOfBoundsException e) {
			throw new FeatureCalcException(e);
		} catch (CreateException e) {
			throw new FeatureCalcException(e);
		}
	}

	@Override
	public String getParamDscr() {
		return pixelList.getBeanDscr();
	}
	
	public PixelStatisticsFromMark getPixelList() {
		return pixelList;
	}


	public void setPixelList(PixelStatisticsFromMark pixelList) {
		this.pixelList = pixelList;
	}
	
	public boolean isIgnoreZero() {
		return ignoreZero;
	}


	public void setIgnoreZero(boolean ignoreZero) {
		this.ignoreZero = ignoreZero;
	}
}
