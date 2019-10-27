package ch.ethz.biol.cell.sgmn.objmask.watershed.encoding;

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

import org.anchoranalysis.image.extent.Extent;
import org.anchoranalysis.image.objmask.ObjMask;
import org.anchoranalysis.image.voxel.buffer.SlidingBuffer;
import org.anchoranalysis.image.voxel.buffer.VoxelBuffer;
import org.anchoranalysis.image.voxel.nghb.BigNghb;
import org.anchoranalysis.image.voxel.nghb.IProcessAbsolutePoint;
import org.anchoranalysis.image.voxel.nghb.IProcessAbsolutePointObjectMask;
import org.anchoranalysis.image.voxel.nghb.Nghb;
import org.anchoranalysis.image.voxel.nghb.SmallNghb;
import org.anchoranalysis.image.voxel.nghb.iterator.PointExtntIterator;
import org.anchoranalysis.image.voxel.nghb.iterator.PointIterator;
import org.anchoranalysis.image.voxel.nghb.iterator.PointObjMaskIterator;

public class SteepestCalc {

	private boolean do3D = false;
	private boolean bigNghb = false;	// If true we use 8-Connectivity instead of 4, and 26-connectivity instead of 6 in 3D
	
	private PointTester pt;
	private PointIterator pointIterator;
	
	private Nghb nghb;
	
	// Without mask
	public SteepestCalc( SlidingBuffer<?> rbb, WatershedEncoding encoder, boolean do3D, boolean bigNghb ) {
		this.do3D = do3D;
		this.bigNghb = bigNghb;
		this.pt = new PointTester(encoder,rbb);
		this.pointIterator = new PointExtntIterator(rbb.extnt(), pt);
		initNghb();
	}
	
	// Masked
	public SteepestCalc( SlidingBuffer<?> rbb, WatershedEncoding encoder, boolean do3D, boolean bigNghb, ObjMask om ) {
		this.do3D = do3D;
		this.bigNghb = bigNghb;
		this.pt = new PointTester(encoder,rbb);
		this.pointIterator = new PointObjMaskIterator(pt, om);
		initNghb();
	}
	
	private void initNghb() {
		nghb = bigNghb ? new BigNghb() : new SmallNghb();
	}
	
	private static class PointTester implements IProcessAbsolutePoint, IProcessAbsolutePointObjectMask {

		
		private SlidingBuffer<?> rbb;
		private WatershedEncoding encoder;
		
		private int centreVal;
		private int indxBuffer;
		
		private int steepestDrctn;
		private int steepestVal;
		
		
		private VoxelBuffer<?> bb;
		private int zChange;
		
		private Extent extnt;
		
		public PointTester(WatershedEncoding encoder, SlidingBuffer<?> rbb ) {
			this.encoder = encoder;
			this.rbb = rbb;
			this.extnt = rbb.extnt();
		}

		public void initPnt( int pnteVal, int indxBuffer ) {
			this.centreVal = pnteVal;
			this.indxBuffer = indxBuffer;
			this.steepestVal = pnteVal;
			this.steepestDrctn = WatershedEncoding.CODE_MINIMA;
		}


		@Override
		public boolean processPoint( int xChange, int yChange, int x1, int y1) {
			
			int indxChange = extnt.offset(xChange, yChange);
			int gValNghb = bb.getInt( indxBuffer + indxChange );
			
			//assert( gValNghb!= WatershedEncoding.CODE_UNVISITED );
			//assert( gValNghb!= WatershedEncoding.CODE_TEMPORARY );
			
			if (gValNghb==centreVal) {
				steepestDrctn = WatershedEncoding.CODE_PLATEAU;
				return true;
			}
			
			if (gValNghb<steepestVal) {
				steepestVal = gValNghb;
				steepestDrctn = encoder.encodeDirection(xChange, yChange, zChange);
				
				// Check that x+xChange,y+yChange+z+zChange is already visited
				//assert( rbb.bufferRel(zChange).get( indxBuffer+ind)  )
				
				return true;
			}
			
			return false;			
		}
		
		@Override
		public boolean processPoint(int xChange, int yChange, int x1, int y1,
				int objectMaskOffset) {
			return processPoint(xChange, yChange, x1, y1);
		}

		public int getSteepestDrctn() {
			return steepestDrctn;
		}

		@Override
		public void notifyChangeZ(int zChange, int z1) {
			this.bb = rbb.bufferRel(zChange);
			this.zChange = zChange;
		}

		@Override
		public void notifyChangeZ(int zChange, int z1,
				ByteBuffer objectMaskBuffer) {
			notifyChangeZ(zChange, z1);
		}

		

	}
	
	// Calculates the steepest descent
	public int calcSteepestDescent( int x, int y, int z, int val, int indxBuffer ) {
		
		this.pointIterator.initPnt(x, y, z);
		this.pt.initPnt(val, indxBuffer);
		
		nghb.processAllPointsInNghb(do3D, pointIterator);
		
		return pt.getSteepestDrctn();
	}
}