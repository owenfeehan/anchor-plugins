package ch.ethz.biol.cell.imageprocessing.chnl.provider;

/*
 * #%L
 * anchor-plugin-image
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


import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.annotation.Optional;
import org.anchoranalysis.bean.annotation.SkipInit;
import org.anchoranalysis.bean.shared.params.keyvalue.KeyValueParamsProvider;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.error.InitException;
import org.anchoranalysis.core.params.KeyValueParams;
import org.anchoranalysis.feature.bean.Feature;
import org.anchoranalysis.feature.init.FeatureInitParams;
import org.anchoranalysis.image.bean.provider.BinaryImgChnlProvider;
import org.anchoranalysis.image.bean.provider.ChnlProvider;
import org.anchoranalysis.image.bean.provider.HistogramProvider;
import org.anchoranalysis.image.binary.BinaryChnl;
import org.anchoranalysis.image.chnl.Chnl;
import org.anchoranalysis.image.chnl.factory.ChnlFactoryByte;
import org.anchoranalysis.image.extent.BoundingBox;
import org.anchoranalysis.image.feature.pixelwise.score.PixelScoreFeatureCalcParams;
import org.anchoranalysis.image.histogram.Histogram;
import org.anchoranalysis.image.init.ImageInitParams;
import org.anchoranalysis.image.objmask.ObjMask;
import org.anchoranalysis.image.voxel.box.VoxelBox;
import org.anchoranalysis.image.voxel.box.VoxelBoxList;
import org.anchoranalysis.image.voxel.box.VoxelBoxWrapper;

import ch.ethz.biol.cell.mpp.nrg.feature.pixelwise.createvoxelbox.CreateVoxelBoxFromPixelwiseFeature;
import ch.ethz.biol.cell.mpp.nrg.feature.pixelwise.createvoxelbox.CreateVoxelBoxFromPixelwiseFeatureWithMask;

public class ChnlProviderPixelScore extends ChnlProvider {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	// START BEAN PROPERTIES
	@BeanField
	private ChnlProvider intensityProvider;
	
	@BeanField @Optional
	private ChnlProvider gradientProvider;
	
	@BeanField @Optional
	private BinaryImgChnlProvider maskProvider;
	
	@BeanField @SkipInit
	private Feature<PixelScoreFeatureCalcParams> pixelScore;
	
	@BeanField
	private List<ChnlProvider> listChnlProviderExtra = new ArrayList<>();
	
	@BeanField
	private List<HistogramProvider> listHistogramProviderExtra = new ArrayList<>();
	
	@BeanField @Optional
	private KeyValueParamsProvider keyValueParamsProvider;
	// END BEAN PROPERTIES
	
	private VoxelBoxList createVoxelBoxList( Chnl chnlIntensity ) throws CreateException {
		
		VoxelBoxList listOut = new VoxelBoxList();
		
		listOut.add( chnlIntensity.getVoxelBox() );
		
		if (gradientProvider!=null) {
			listOut.add( gradientProvider.create().getVoxelBox() );
		}
		for( ChnlProvider chnlProvider : listChnlProviderExtra ) {
			VoxelBoxWrapper vbExtra = chnlProvider!=null ? chnlProvider.create().getVoxelBox() : null;
			listOut.add(vbExtra);
		}
		return listOut;
	}
	
	
	private ObjMask createMaskOrNull() throws CreateException {
		if (maskProvider==null) {
			return null;
		}
		
		BinaryChnl binaryChnlMask = maskProvider.create();
		Chnl chnlMask = binaryChnlMask.getChnl();
		
		return new ObjMask(
			new BoundingBox( chnlMask.getDimensions().getExtnt()),
			chnlMask.getVoxelBox().asByte(),
			binaryChnlMask.getBinaryValues()
		);
	}
	

	@Override
	public void onInit(ImageInitParams so) throws InitException {
		super.onInit(so);
		pixelScore.initRecursive( new FeatureInitParams(), getLogger() );
	}
	
	@Override
	public Chnl create() throws CreateException {
		
		Chnl chnlIntensity = intensityProvider.create();
				
		VoxelBoxList listVb = createVoxelBoxList( chnlIntensity);
		List<Histogram> listHistExtra = ProviderBeanUtilities.listFromBeans(listHistogramProviderExtra);
		KeyValueParams kpv;
		if (keyValueParamsProvider!=null) {
			kpv = keyValueParamsProvider.create();
		} else {
			kpv = new KeyValueParams();
		}

		ObjMask objMask = createMaskOrNull();
		
		VoxelBox<ByteBuffer> vbPixelScore;
		if (objMask!=null) {
			CreateVoxelBoxFromPixelwiseFeatureWithMask creator = new CreateVoxelBoxFromPixelwiseFeatureWithMask(
				listVb,
				getSharedObjects().getRandomNumberGenerator(),
				kpv,
				listHistExtra
			);
			
			vbPixelScore = creator.createVoxelBoxFromPixelScore(pixelScore,objMask, getLogger() );
			
		} else {
			CreateVoxelBoxFromPixelwiseFeature creator = new CreateVoxelBoxFromPixelwiseFeature(
				listVb,
				getSharedObjects().getRandomNumberGenerator(),
				kpv,
				listHistExtra
			);
				
			vbPixelScore = creator.createVoxelBoxFromPixelScore(pixelScore, getLogger() );
		}
		
		return new ChnlFactoryByte().create(vbPixelScore, chnlIntensity.getDimensions().getRes());
	}
	
	public ChnlProvider getIntensityProvider() {
		return intensityProvider;
	}

	public void setIntensityProvider(ChnlProvider intensityProvider) {
		this.intensityProvider = intensityProvider;
	}

	public ChnlProvider getGradientProvider() {
		return gradientProvider;
	}

	public void setGradientProvider(ChnlProvider gradientProvider) {
		this.gradientProvider = gradientProvider;
	}

	public BinaryImgChnlProvider getMaskProvider() {
		return maskProvider;
	}

	public void setMaskProvider(BinaryImgChnlProvider maskProvider) {
		this.maskProvider = maskProvider;
	}

	public Feature<PixelScoreFeatureCalcParams> getPixelScore() {
		return pixelScore;
	}

	public void setPixelScore(Feature<PixelScoreFeatureCalcParams> pixelScore) {
		this.pixelScore = pixelScore;
	}

	public List<ChnlProvider> getListChnlProviderExtra() {
		return listChnlProviderExtra;
	}

	public void setListChnlProviderExtra(List<ChnlProvider> listChnlProviderExtra) {
		this.listChnlProviderExtra = listChnlProviderExtra;
	}

	public List<HistogramProvider> getListHistogramProviderExtra() {
		return listHistogramProviderExtra;
	}

	public void setListHistogramProviderExtra(
			List<HistogramProvider> listHistogramProviderExtra) {
		this.listHistogramProviderExtra = listHistogramProviderExtra;
	}


	public KeyValueParamsProvider getKeyValueParamsProvider() {
		return keyValueParamsProvider;
	}


	public void setKeyValueParamsProvider(KeyValueParamsProvider keyValueParamsProvider) {
		this.keyValueParamsProvider = keyValueParamsProvider;
	}


}
