package org.anchoranalysis.plugin.image.feature.bean.object.single.intensity;

/*-
 * #%L
 * anchor-plugin-image-feature
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
import org.anchoranalysis.feature.bean.Feature;
import org.anchoranalysis.feature.cache.ChildCacheName;
import org.anchoranalysis.feature.cache.SessionInput;
import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.image.channel.Channel;
import org.anchoranalysis.image.feature.histogram.FeatureInputHistogram;
import org.anchoranalysis.image.feature.histogram.Mean;
import org.anchoranalysis.image.feature.object.input.FeatureInputSingleObject;

import lombok.Getter;
import lombok.Setter;

/**
 * Calculates a statistic from the intensity values covered by a single object-mask in a channel.
 * <p>
 * Specifically, a histogram of intensity-values is constructed for the region covered by the object in
 * one specific channnel in the NRG-stack (specified by <b>nrgIndex</b>).
 * <p>
 * Then a customizable {@link org.anchoranalysis.image.feature.bean.FeatureHistogram} (specified by <b>item</b>) extracts a statistic
 * from the histogram. By default, the <i>mean</i> is calculated.
 * 
 * @author Owen Feehan
 *
 */
public class Intensity extends FeatureNrgChnl {

	// START BEAN PROPERTIES
	/** Feature to apply to the histogram */
	@BeanField @Getter @Setter
	private Feature<FeatureInputHistogram> item = new Mean();
	
	/** Iff TRUE, zero-valued voxels are excluded from the histogram */
	@BeanField @Getter @Setter
	private boolean excludeZero = false;
	// END BEAN PROEPRTIES
	
	@Override
	protected double calcForChnl(SessionInput<FeatureInputSingleObject> input, Channel chnl) throws FeatureCalcException {
		return input.forChild().calc(
			item,
			new CalculateHistogramForNrgChnl(excludeZero, getNrgIndex(), chnl),
			cacheName()
		);
	}
	
	private ChildCacheName cacheName() {
		return new ChildCacheName(
			Intensity.class,
			String.valueOf(excludeZero) + "_" + String.valueOf(getNrgIndex())
		);
	}
}
