package org.anchoranalysis.plugin.image.feature.bean.obj.single.intensity;

import org.anchoranalysis.feature.cache.calculation.FeatureCalculation;
import org.anchoranalysis.image.chnl.Chnl;
import org.anchoranalysis.image.feature.histogram.FeatureInputHistogram;
import org.anchoranalysis.image.feature.objmask.FeatureInputSingleObj;
import org.anchoranalysis.image.histogram.Histogram;
import org.anchoranalysis.image.histogram.HistogramFactoryUtilities;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

class CalculateHistogramForNrgChnl extends FeatureCalculation<FeatureInputHistogram, FeatureInputSingleObj> {

	private boolean excludeZero = false;
	private int nrgIndex;
	private Chnl chnl;

	/**
	 * Constructor
	 * 
	 * @param excludeZero iff TRUE zero-intensity values are excluded from the histogram, otherwise they are included
	 * @param nrgIndex an index uniquely identifying the channel
	 * @param chnl the channel corresponding to nrgIndex
	 */
	public CalculateHistogramForNrgChnl(boolean excludeZero, int nrgIndex, Chnl chnl) {
		super();
		this.excludeZero = excludeZero;
		this.nrgIndex = nrgIndex;
		this.chnl = chnl;
	}
	
	@Override
	protected FeatureInputHistogram execute(FeatureInputSingleObj params) {

		Histogram hist = HistogramFactoryUtilities.createHistogramIgnoreZero(
			chnl,
			params.getObjMask(),
			excludeZero
		);
		
		return new FeatureInputHistogram(
			hist,
			params.getResOptional()
		);
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) { return false; }
		if (obj == this) { return true; }
		if (obj.getClass() != getClass()) {
			return false;
		}
		CalculateHistogramForNrgChnl rhs = (CalculateHistogramForNrgChnl) obj;
		return new EqualsBuilder()
			.append(excludeZero, rhs.excludeZero)
			.append(nrgIndex, rhs.nrgIndex)
            .isEquals();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder()
			.append(excludeZero)
			.append(nrgIndex)
			.toHashCode();
	}
}
