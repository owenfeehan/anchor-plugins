package ch.ethz.biol.cell.imageprocessing.objmask.provider;

/*-
 * #%L
 * anchor-plugin-image
 * %%
 * Copyright (C) 2010 - 2019 Owen Feehan, ETH Zurich, University of Zurich, Hoffmann la Roche
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

import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.image.bean.provider.ObjMaskProviderOne;
import org.anchoranalysis.image.binary.voxel.BinaryVoxelBox;
import org.anchoranalysis.image.objectmask.ObjectMask;
import org.anchoranalysis.image.objectmask.ObjectCollection;
import org.anchoranalysis.image.objectmask.factory.CreateFromConnectedComponentsFactory;

/**
 * Ensures each obj in a collection is a connected-component, decomposing it if necessary
 *  into multiple objects
 * 
 * @author FEEHANO
 *
 */
public class ObjMaskProviderConnectedComponentsObjs extends ObjMaskProviderOne {

	// START BEAN PROPERTIES
	/** if TRUE, uses 8 neighbourhood instead of 4, and similarly in 3d */
	@BeanField
	private boolean bigNghb = false;
	// END BEAN PROPERTIES
	
	@Override
	public ObjectCollection createFromObjs(ObjectCollection objsCollection) throws CreateException {
				
		CreateFromConnectedComponentsFactory createObjMasks = new CreateFromConnectedComponentsFactory(bigNghb, 1);
		
		return objsCollection.flatMapWithException(
			CreateException.class,
			om -> createObjs3D(om, createObjMasks)
		);
	}
	
	private ObjectCollection createObjs3D(
		ObjectMask omUnconnected,
		CreateFromConnectedComponentsFactory createObjMasks
	) throws CreateException {
		
		ObjectCollection objs = createObjsFromMask( omUnconnected.binaryVoxelBox(), createObjMasks );
		
		// Adjust the crnr of each object, by adding on the original starting point of our object-mask
		return objs.shiftBy(
			omUnconnected.getBoundingBox().getCrnrMin()
		);
	}
	
	private ObjectCollection createObjsFromMask( BinaryVoxelBox<ByteBuffer> vb, CreateFromConnectedComponentsFactory createObjMasks ) throws CreateException {
		return createObjMasks.createConnectedComponents(vb );
	}

	public boolean isBigNghb() {
		return bigNghb;
	}

	public void setBigNghb(boolean bigNghb) {
		this.bigNghb = bigNghb;
	}

}
