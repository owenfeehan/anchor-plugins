package ch.ethz.biol.cell.sgmn.objmask.watershed.minimaimposition.grayscalereconstruction;

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
import org.anchoranalysis.image.voxel.box.VoxelBox;
import org.anchoranalysis.image.voxel.buffer.SlidingBuffer;
import org.anchoranalysis.image.voxel.buffer.VoxelBuffer;
import org.anchoranalysis.image.voxel.nghb.BigNghb;
import org.anchoranalysis.image.voxel.nghb.IProcessAbsolutePoint;
import org.anchoranalysis.image.voxel.nghb.Nghb;
import org.anchoranalysis.image.voxel.nghb.iterator.PointExtntIterator;
import org.anchoranalysis.image.voxel.nghb.iterator.PointIterator;


// Performs grayscale erosion on a point using a 3x3 FLAT structuring element
// The same as MINIMUM filter
// NOT EFFICIENTLY IMPLEMENTED
public class GrayscaleErosion {

	private boolean do3D = false;
	
	private PointTester pt;
	private PointIterator pointIterator;
	
	// Without mask
	public GrayscaleErosion( SlidingBuffer<ByteBuffer> rbb, boolean do3D ) {
		this.do3D = do3D;
		this.pt = new PointTester(rbb);
		this.pointIterator = new PointExtntIterator( rbb.extnt(), pt);
	}
	
	// Masked
//	public SteepestCalc( Extnt e, SlidingBuffer<ByteBuffer> rbb, WatershedEncoding encoder, boolean do3D, boolean bigNghb, ObjMask om ) {
//		this.do3D = do3D;
//		this.bigNghb = bigNghb;
//		this.pt = new PointTester(e,encoder,rbb);
//		this.pointIterator = new PointObjMaskIterator(pt, om);
//	}
	
	
	private static class PointTester implements IProcessAbsolutePoint {

		private SlidingBuffer<ByteBuffer> rbb;

		// Current bytebuffer
		private VoxelBuffer<ByteBuffer> bb;
		private int indx;
		
		// Current minima
		private int minima;
		
		public PointTester(SlidingBuffer<ByteBuffer> rbb) {
			super();
			this.rbb = rbb;
		}

		public void reset( int indx, int exstVal ) {
			// Undefined
			minima = exstVal;
			this.indx = indx;
		}
		
		@Override
		public void notifyChangeZ(int zChange, int z) {
			this.bb = rbb.bufferRel(zChange);
		}

		@Override
		public boolean processPoint(int xChange, int yChange, int x1, int y1) {

			// We can replace with local index changes
			int indxChange = rbb.extnt().offset(xChange, yChange);
			
			int val = bb.getInt(indx+indxChange);
			
			if (val < minima) {
				minima = val;
				return true;
			}
			return false;
		}

		public int getMinima() {
			return minima;
		}
	}
	
	
	// The sliding buffer must be centred at the current value of z
	public int grayscaleErosion( int x, int y, int z, SlidingBuffer<ByteBuffer> buffer, int indx, int exstVal ) {
		
		this.pointIterator.initPnt(x, y, z);
		this.pt.reset( indx, exstVal );
		
		// Makes sure that it includes its centre point
		Nghb nghb = new BigNghb(false);
		nghb.processAllPointsInNghb(do3D, pointIterator);
		
		return pt.getMinima();
	}
	
	// Returns a bool, if at least one pixel changed
	public static boolean grayscaleErosion( VoxelBox<ByteBuffer> vbIn, VoxelBox<ByteBuffer> vbOut ) {
		
		
		Extent e = vbIn.extnt();
		// We iterate

		SlidingBuffer<ByteBuffer> sb = new SlidingBuffer<>(vbIn);
		sb.init();
		
		GrayscaleErosion ge = new GrayscaleErosion(sb, vbIn.extnt().getZ() > 1 );
		
		boolean pixelChanged = false;
		
		for (int z=0; z<e.getZ(); z++) {
			
			ByteBuffer bbOut = vbOut.getPixelsForPlane(z).buffer();
			
			int indx = 0;
			for (int y=0; y<e.getY(); y++) {
				for (int x=0; x<e.getX(); x++) {

					
					int valExst = sb.getCentre().getInt(indx);
					
					int valNew = ge.grayscaleErosion(x, y, z, sb, indx, valExst);
					bbOut.put(indx, (byte) valNew );
					
					if (valNew!=valExst) {
						pixelChanged = true;
					}
					
					indx++;
				}
			}
			
			sb.shift();
		}
		
		return pixelChanged;
	}
}
