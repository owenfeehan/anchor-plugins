package ch.ethz.biol.cell.imageprocessing.chnl.provider;

import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.image.bean.provider.BinaryImgChnlProvider;
import org.anchoranalysis.image.bean.provider.ChnlProviderOne;
import org.anchoranalysis.image.binary.BinaryChnl;
import org.anchoranalysis.image.chnl.Chnl;

public abstract class ChnlProviderOneMask extends ChnlProviderOne {

	// START BEAN PROPERTIES
	@BeanField
	private BinaryImgChnlProvider mask;
	// END BEAN PROPERTIES
	
	@Override
	public Chnl createFromChnl(Chnl chnl) throws CreateException {
		BinaryChnl maskChnl = mask.create();
		DimChecker.check(chnl, maskChnl);
		return createFromMaskedChnl(chnl, maskChnl);
	}
	
	protected abstract Chnl createFromMaskedChnl(Chnl chnl, BinaryChnl mask) throws CreateException;

	public BinaryImgChnlProvider getMask() {
		return mask;
	}

	public void setMask(BinaryImgChnlProvider mask) {
		this.mask = mask;
	}
}
