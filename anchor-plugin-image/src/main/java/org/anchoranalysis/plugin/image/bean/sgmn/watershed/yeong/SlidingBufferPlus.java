package org.anchoranalysis.plugin.image.bean.sgmn.watershed.yeong;

import java.util.Optional;

import org.anchoranalysis.core.geometry.Point3i;
import org.anchoranalysis.image.objmask.ObjMask;
import org.anchoranalysis.image.voxel.box.VoxelBox;
import org.anchoranalysis.image.voxel.buffer.SlidingBuffer;

import ch.ethz.biol.cell.sgmn.objmask.watershed.encoding.EncodedIntBuffer;
import ch.ethz.biol.cell.sgmn.objmask.watershed.encoding.EncodedVoxelBox;
import ch.ethz.biol.cell.sgmn.objmask.watershed.encoding.SteepestCalc;

/** A sliding-buffer enhanced with other elements of internal state used to "visit" a pixel */
final class SlidingBufferPlus {
	
	private final SlidingBuffer<?> rbb;
	private final FindEqualVoxels findEqualVoxels;
	private final SteepestCalc steepestCalc;
	
	private final EncodedVoxelBox matS;
	private final Optional<MinimaStore> minimaStore;
	
	public SlidingBufferPlus(VoxelBox<?> vbImg, EncodedVoxelBox matS, Optional<ObjMask> mask, Optional<MinimaStore> minimaStore) {

		this.matS = matS;
		this.minimaStore = minimaStore;
		
		this.rbb = new SlidingBuffer<>( vbImg );
		
		boolean do3D = vbImg.extnt().getZ()>1;
		this.findEqualVoxels = new FindEqualVoxels( vbImg, matS, do3D, mask );
		this.steepestCalc = new SteepestCalc(rbb,matS.getEncoding(), do3D ,true, mask );
	}
	
	public void visitPixel(
		Point3i pnt,
		EncodedIntBuffer bbS
	) {
		int indxBuffer = rbb.extnt().offsetSlice(pnt);
		
		// Exit early if this voxel has already been visited
		if (!bbS.isUnvisited(indxBuffer)) {
			return;
		}
		
		// We get the value of g
		int gVal = rbb.getCentre().getInt(indxBuffer);
		
		// Calculate steepest descent. -1 indicates that there is no steepest descent
		int chainCode = steepestCalc.calcSteepestDescent(pnt,gVal,indxBuffer);
		
		if (matS.isMinima(chainCode)) {
			// Treat as local minima
			bbS.putCode(indxBuffer, chainCode);	
			
			if (minimaStore.isPresent()) {
				minimaStore.get().addDuplicated(pnt);
			}
			
		} else if (matS.isPlateau(chainCode)) {

			new MakePlateauLowerComplete(
				findEqualVoxels.createPlateau(pnt),
				findEqualVoxels.isDo3D()
			).makeBufferLowerCompleteForPlateau(
				matS,
				minimaStore
			);

		} else {
			// Record steepest
			bbS.putCode(indxBuffer,chainCode);
		}
	}

	public EncodedIntBuffer getSPlane(int z) {
		return matS.getPixelsForPlane(z);
	}

	public SlidingBuffer<?> getSlidingBuffer() {
		return rbb;
	}
}