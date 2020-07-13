package ch.ethz.biol.cell.sgmn.objmask;

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
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.image.bean.nonbean.error.SgmnFailedException;
import org.anchoranalysis.image.bean.segmentation.object.ObjectSegmentation;
import org.anchoranalysis.image.bean.segmentation.object.ObjectSegmentationOne;
import org.anchoranalysis.image.channel.Channel;
import org.anchoranalysis.image.object.ObjectCollection;
import org.anchoranalysis.image.object.ObjectMask;
import org.anchoranalysis.image.seed.SeedCollection;
import org.anchoranalysis.plugin.image.bean.sgmn.watershed.minima.MinimaImposition;

import lombok.Getter;
import lombok.Setter;


// Imposes minima only in seed locations on the input channel, and performs the segmentation
public class ObjMaskSgmnMinimaImposition extends ObjectSegmentationOne {

	// START BEAN PROPERTIES
	@BeanField @Getter @Setter
	private MinimaImposition minimaImposition;
	// END BEAN PROPERTIES

	@Override
	public ObjectCollection sgmn(
		Channel chnl,
		Optional<ObjectMask> mask,
		Optional<SeedCollection> seeds,
		ObjectSegmentation sgmn
	) throws SgmnFailedException {

		if (!seeds.isPresent()) {
			throw new SgmnFailedException("seeds must be present");
		}
				
		try {
			return sgmn.sgmn(
				chnlWithImposedMinima(chnl, seeds.get(), mask),
				mask,
				seeds
			);
			
		} catch (OperationFailedException e) {
			throw new SgmnFailedException(e);
		}
	}
	
	private Channel chnlWithImposedMinima(Channel chnl, SeedCollection seeds, Optional<ObjectMask> objMask) throws OperationFailedException {
		if (!seeds.isEmpty()) {
			return minimaImposition.imposeMinima(chnl, seeds, objMask);
		} else {
			return chnl;
		}
	}
}
