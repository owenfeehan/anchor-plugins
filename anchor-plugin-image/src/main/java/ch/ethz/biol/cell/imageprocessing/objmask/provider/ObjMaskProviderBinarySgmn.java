package ch.ethz.biol.cell.imageprocessing.objmask.provider;

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
import java.util.Optional;

import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.image.bean.sgmn.binary.BinarySgmn;
import org.anchoranalysis.image.bean.sgmn.binary.BinarySgmnParameters;
import org.anchoranalysis.image.binary.voxel.BinaryVoxelBox;
import org.anchoranalysis.image.channel.Channel;
import org.anchoranalysis.image.objectmask.ObjectMask;
import org.anchoranalysis.image.objectmask.ObjectCollection;
import org.anchoranalysis.image.sgmn.SgmnFailedException;
import org.anchoranalysis.image.voxel.box.VoxelBox;
import org.anchoranalysis.image.voxel.box.VoxelBoxWrapper;

public class ObjMaskProviderBinarySgmn extends ObjMaskProviderOneChnlSource {

	// START BEAN PROPERTIES
	@BeanField
	private BinarySgmn binarySgmn;
	// END BEAN PROPERTIES

	@Override
	protected ObjectCollection createFromObjs(ObjectCollection objsSrc, Channel chnlSrc) throws CreateException {
		try {
			return objsSrc.map( om->
				sgmnObject(om, chnlSrc)
			);
		} catch (SgmnFailedException e) {
			throw new CreateException(e);
		}
	}
	
	private ObjectMask sgmnObject(ObjectMask om, Channel chnlSrc) throws SgmnFailedException {
		VoxelBox<?> vb = chnlSrc.getVoxelBox().any().region(om.getBoundingBox(),true);
		
		BinaryVoxelBox<ByteBuffer> bvb = binarySgmn.sgmn(
			new VoxelBoxWrapper(vb),
			new BinarySgmnParameters(),
			Optional.of(
				new ObjectMask(om.getVoxelBox())
			)
		);
				
		return new ObjectMask(om.getBoundingBox(), bvb);	
	}

	public BinarySgmn getBinarySgmn() {
		return binarySgmn;
	}

	public void setBinarySgmn(BinarySgmn binarySgmn) {
		this.binarySgmn = binarySgmn;
	}
}
