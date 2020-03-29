package ch.ethz.biol.cell.imageprocessing.objmask.provider;

import org.anchoranalysis.anchor.mpp.bean.cfg.CfgProvider;
import org.anchoranalysis.anchor.mpp.bean.regionmap.RegionMap;
import org.anchoranalysis.anchor.mpp.cfg.Cfg;
import org.anchoranalysis.anchor.mpp.regionmap.RegionMapSingleton;

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


import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.image.binary.values.BinaryValuesByte;
import org.anchoranalysis.image.extent.ImageDim;
import org.anchoranalysis.image.objmask.ObjMaskCollection;

// Creates some object masks from a cfg
public class ObjMaskProviderCfg extends ObjMaskProviderDimensions {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	// START BEAN PROPERTIES
	@BeanField
	private CfgProvider cfgProvider;
	
	@BeanField
	private int regionID = 0;
	
	@BeanField
	private RegionMap regionMap = RegionMapSingleton.instance();
	// END BEAN PROPERTIES

	@Override
	public ObjMaskCollection create() throws CreateException {
		
		Cfg cfg = cfgProvider.create();
		
		ImageDim dims = createDims();
		
		return cfg.calcMask(
			dims,
			regionMap.membershipWithFlagsForIndex(regionID),
			BinaryValuesByte.getDefault(),
			null
		).collectionObjMask();
	}

	public CfgProvider getCfgProvider() {
		return cfgProvider;
	}

	public void setCfgProvider(CfgProvider cfgProvider) {
		this.cfgProvider = cfgProvider;
	}

	public int getRegionID() {
		return regionID;
	}

	public void setRegionID(int regionID) {
		this.regionID = regionID;
	}

}
