package ch.ethz.biol.cell.mpp.nrg.feature.ind;

import org.anchoranalysis.anchor.mpp.bean.regionmap.RegionMap;
import org.anchoranalysis.anchor.mpp.feature.nrg.elem.NRGElemIndCalcParams;
import org.anchoranalysis.anchor.mpp.feature.nrg.elem.NRGElemIndCalcParamsDescriptor;
import org.anchoranalysis.anchor.mpp.regionmap.RegionMapSingleton;

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
import org.anchoranalysis.feature.bean.operator.FeatureSingleElem;
import org.anchoranalysis.feature.cache.CacheableParams;
import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.feature.calc.params.FeatureCalcParams;
import org.anchoranalysis.feature.params.FeatureParamsDescriptor;
import org.anchoranalysis.image.binary.values.BinaryValuesByte;
import org.anchoranalysis.image.feature.objmask.FeatureObjMaskParams;
import org.anchoranalysis.image.objmask.properties.ObjMaskWithProperties;

public class AsObjMask extends FeatureSingleElem {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	// START BEAN PROPERTIES
	@BeanField
	private RegionMap regionMap = RegionMapSingleton.instance();
	
	@BeanField
	private int index = 0;
	// END BEAN PROPERTIES
	
	@Override
	public double calc(CacheableParams<? extends FeatureCalcParams> params) throws FeatureCalcException {
		
		if (params.getParams() instanceof NRGElemIndCalcParams) {
		
			NRGElemIndCalcParams paramsCast = (NRGElemIndCalcParams) params.getParams(); 
			
			 
			
			FeatureObjMaskParams paramsNew;
			ObjMaskWithProperties om = paramsCast.getPxlPartMemo().getMark().calcMask(
				paramsCast.getNrgStack().getDimensions(),
				regionMap.membershipWithFlagsForIndex(index),
				BinaryValuesByte.getDefault()
			);
			
			paramsNew = new FeatureObjMaskParams(
				om.getMask()
			);
			paramsNew.setNrgStack( paramsCast.getNrgStack() );
			
			double ret = getCacheSession().calc(
				getItem(),
				params.changeParams(
					paramsNew
				)
			);
			
			return ret;
			
		} else {
			throw new FeatureCalcException("Not supported for this type of params");
		}
	}

	// We change the default behaviour, as we don't want to give the same paramsFactory
	//   as the item we pass to
	@Override
	public FeatureParamsDescriptor paramType()
			throws FeatureCalcException {
		return NRGElemIndCalcParamsDescriptor.instance;
	}

	@Override
	public String getParamDscr() {
		return getItem().getParamDscr();
	}

	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}

	public RegionMap getRegionMap() {
		return regionMap;
	}

	public void setRegionMap(RegionMap regionMap) {
		this.regionMap = regionMap;
	}


}
