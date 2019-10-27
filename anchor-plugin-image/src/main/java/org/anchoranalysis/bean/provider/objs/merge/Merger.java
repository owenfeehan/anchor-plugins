package org.anchoranalysis.bean.provider.objs.merge;

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
import java.util.ArrayList;
import java.util.List;

import org.anchoranalysis.bean.provider.objs.merge.condition.AfterCondition;
import org.anchoranalysis.bean.provider.objs.merge.condition.BeforeCondition;
import org.anchoranalysis.core.error.InitException;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.core.geometry.Point3d;
import org.anchoranalysis.core.geometry.Point3i;
import org.anchoranalysis.image.binary.values.BinaryValues;
import org.anchoranalysis.image.binary.voxel.BinaryVoxelBox;
import org.anchoranalysis.image.binary.voxel.BinaryVoxelBoxByte;
import org.anchoranalysis.image.extent.BoundingBox;
import org.anchoranalysis.image.extent.Extent;
import org.anchoranalysis.image.extent.ImageRes;
import org.anchoranalysis.image.objmask.ObjMask;
import org.anchoranalysis.image.objmask.ObjMaskCollection;
import org.anchoranalysis.image.objmask.ops.ObjMaskMerger;
import org.anchoranalysis.image.voxel.box.VoxelBox;
import org.anchoranalysis.image.voxel.box.factory.VoxelBoxFactory;


/**
 * Naieve merge algorithm
 * 
 * @author FEEHANO
 *
 */
class Merger {
	
	private BeforeCondition beforeCondition;
	private AfterCondition afterCondition;
	private ImageRes res;
	private boolean replaceWithMidpoint;
	
	private static class MergeParams {
		private int startSrc;
		private int endSrc;
				
		public MergeParams(int startSrc, int endSrc) {
			super();
			this.startSrc = startSrc;
			this.endSrc = endSrc;
		}
		
		public int getStartSrc() {
			return startSrc;
		}

		public int getEndSrc() {
			return endSrc;
		}
	}
	
	public Merger(boolean replaceWithMidpoint, BeforeCondition beforeCondition, AfterCondition afterCondition, ImageRes res) {
		super();
		this.beforeCondition = beforeCondition;
		this.afterCondition = afterCondition;
		this.res = res;
		this.replaceWithMidpoint = replaceWithMidpoint;
	}

	/**
	 * Tries to merge objs (the collection is changed in-place)
	 * 
	 * @param objs the objects to merge
	 * @throws OperationFailedException
	 */
	public ObjMaskCollection tryMerge( ObjMaskCollection objs ) throws OperationFailedException {
		
		List<MergeParams> stack = new ArrayList<>();
		MergeParams mergeParams = new MergeParams(0,0);
		
		stack.add(mergeParams);
		
		while( !stack.isEmpty() ) {
			MergeParams params = stack.remove(0);
			tryMergeOnIndices(objs, params, stack);
		}
		
		return objs;
	}
		
	/**
	 * Tries to merge a particular subset of objects in objs based upon the parameters in mergeParams
	 * 
	 * @param objs the entire set of objects
	 * @param mergeParams parameters that determine which objects are considered for merge
	 * @param stack the entire list of future parameters to also be considered
	 * @throws OperationFailedException
	 */
	private void tryMergeOnIndices( ObjMaskCollection objs, MergeParams mergeParams, List<MergeParams> stack ) throws OperationFailedException {
		
		try {
			afterCondition.init();
		} catch (InitException e) {
			throw new OperationFailedException(e);
		}
		
		for( int i=mergeParams.getStartSrc(); i<objs.size(); i++ ) {
			for( int j=mergeParams.getEndSrc(); j<objs.size(); j++ ) {
				
				if (i==j) {
					continue;
				}
				
				ObjMask omSrc = objs.get(i);
				ObjMask omDest = objs.get(j);
				
				ObjMask omMerge = tryMerge( omSrc, omDest );
				if (omMerge==null) {
					continue;
				}
				
				if (i<j) {
					objs.remove(j);
					objs.remove(i);
				} else {
					objs.remove(i);
					objs.remove(j);
				}
				
				objs.add(omMerge);
				
				int startPos = Math.max(i-1,0);
				stack.add( new MergeParams(startPos, startPos) );
				
				break;
			}
		}
	}

	private ObjMask tryMerge( ObjMask omSrc, ObjMask omDest ) throws OperationFailedException {
		
		if(!beforeCondition.accept(omSrc, omDest, res)) {
			return null;
		}
		
		// Do merge
		ObjMask omMerge;
		if (replaceWithMidpoint) {
			Point3d pntNew = Point3d.midPointBetween( omSrc.getBoundingBox().midpoint(), omDest.getBoundingBox().midpoint() );
			omMerge = createSinglePixelObjMask( new Point3i( pntNew ) );
			
		} else {
			omMerge = ObjMaskMerger.merge(omSrc, omDest );
		}

		if(!afterCondition.accept(omSrc, omDest, omMerge, res)) {
			return null;
		}
		
		return omMerge;
	}
		
	private static ObjMask createSinglePixelObjMask( Point3i pnt ) {
		Extent e = new Extent(1,1,1);
		VoxelBox<ByteBuffer> vb = VoxelBoxFactory.getByte().create( e );
		BinaryVoxelBox<ByteBuffer> bvb = new BinaryVoxelBoxByte(vb, BinaryValues.getDefault() );
		bvb.setAllPixelsToOn();
		BoundingBox bbox = new BoundingBox(pnt, e);
		
		return new ObjMask( bbox, bvb );
	}
}
