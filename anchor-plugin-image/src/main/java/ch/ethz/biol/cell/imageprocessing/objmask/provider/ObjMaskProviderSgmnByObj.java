package ch.ethz.biol.cell.imageprocessing.objmask.provider;

import java.util.Optional;

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


import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.image.bean.sgmn.objmask.ObjMaskSgmn;
import org.anchoranalysis.image.chnl.Chnl;
import org.anchoranalysis.image.objmask.ObjMask;
import org.anchoranalysis.image.objmask.ObjMaskCollection;
import org.anchoranalysis.image.sgmn.SgmnFailedException;

public class ObjMaskProviderSgmnByObj extends ObjMaskProviderOneChnlSource {

	// START BEAN PROPERTIES
	@BeanField
	private ObjMaskSgmn sgmn;
	// END BEAN PROPERTIES

	@Override
	public ObjMaskCollection createFromObjs( ObjMaskCollection objsSrc, Chnl chnlToSgmn ) throws CreateException {
		
		ObjMaskCollection objsOut = new ObjMaskCollection();
		
		for( ObjMask om : objsSrc ) {
			try {
				objsOut.addAll(
					sgmn.sgmn(
						chnlToSgmn,
						Optional.of(om),
						Optional.empty()
					)
				);
			} catch (SgmnFailedException e) {
				throw new CreateException(e);
			}
		}
		
		return objsOut;
	}

	public ObjMaskSgmn getSgmn() {
		return sgmn;
	}

	public void setSgmn(ObjMaskSgmn sgmn) {
		this.sgmn = sgmn;
	}
}
