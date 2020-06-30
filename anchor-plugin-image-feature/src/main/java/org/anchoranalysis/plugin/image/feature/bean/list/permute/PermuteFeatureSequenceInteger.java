package org.anchoranalysis.plugin.image.feature.bean.list.permute;

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

import java.util.ArrayList;

import org.anchoranalysis.bean.BeanInstanceMap;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.error.BeanMisconfiguredException;
import org.anchoranalysis.bean.permute.property.PermutePropertySequenceInteger;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.feature.bean.Feature;
import org.anchoranalysis.feature.bean.list.FeatureList;
import org.anchoranalysis.feature.input.FeatureInputNRGStack;
import ch.ethz.biol.cell.mpp.nrg.feature.objmask.NRGParamThree;

/**
 * Permutes a property on a feature with a sequence of integers.
 * 
 * @author Owen Feehan
 *
 */
public abstract class PermuteFeatureSequenceInteger extends PermuteFeatureBase<FeatureInputNRGStack> {

	// START BEAN PROPERTIES
	@BeanField
	private String paramPrefix;
	
	@BeanField
	private PermutePropertySequenceInteger permuteProperty;
	// END BEAN PROPERTIES
	
	// Possible defaultInstances for beans......... saved from checkMisconfigured for delayed checks elsewhere
	private BeanInstanceMap defaultInstances;
	
	@Override
	public void checkMisconfigured(BeanInstanceMap defaultInstances) throws BeanMisconfiguredException {
		super.checkMisconfigured(defaultInstances);
		this.defaultInstances = defaultInstances;
	}
		
	@Override
	protected FeatureList<FeatureInputNRGStack> createPermutedFeaturesFor(Feature<FeatureInputNRGStack> feature) throws CreateException {
		PermuteFeature<Integer,FeatureInputNRGStack> delegate = createDelegate(feature);
		
		configurePermutePropertyOnDelegate(delegate);
		try {
			delegate.checkMisconfigured( defaultInstances );
		} catch (BeanMisconfiguredException e) {
			throw new CreateException(e);
		}
				
		return delegate.create();
	}
	
	protected abstract PermuteFeature<Integer,FeatureInputNRGStack> createDelegate(Feature<FeatureInputNRGStack> feature) throws CreateException;
	
	protected abstract PermutePropertySequenceInteger configurePermuteProperty( PermutePropertySequenceInteger permuteProperty );
	
	protected Feature<FeatureInputNRGStack> createNRGParam(
		String suffix,
		boolean appendNumber
	) {
		NRGParamThree paramMean = new NRGParamThree();
		paramMean.setIdLeft(paramPrefix);
		if (appendNumber) {
			paramMean.setIdMiddle(
				Integer.toString(permuteProperty.getSequence().getStart())
			);
		} else {
			paramMean.setIdMiddle("");
		}
		paramMean.setIdRight(suffix);
		return paramMean;
	}

	private void configurePermutePropertyOnDelegate( PermuteFeature<Integer,FeatureInputNRGStack> delegate ) {
		PermutePropertySequenceInteger permutePropertyConfigured = configurePermuteProperty(
			(PermutePropertySequenceInteger) permuteProperty.duplicateBean()
		);
		
		delegate.setListPermuteProperty( new ArrayList<>() );
		delegate.getListPermuteProperty().add( permutePropertyConfigured );		
	}
	
	public String getParamPrefix() {
		return paramPrefix;
	}

	public void setParamPrefix(String paramPrefix) {
		this.paramPrefix = paramPrefix;
	}
	
	public PermutePropertySequenceInteger getPermuteProperty() {
		return permuteProperty;
	}

	public void setPermuteProperty(PermutePropertySequenceInteger permuteProperty) {
		this.permuteProperty = permuteProperty;
	}
}
