package org.anchoranalysis.plugin.mpp.sgmn.cfg.bean.kernel.independent;

/*
 * #%L
 * anchor-mpp
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


import java.util.List;
import java.util.Optional;

import org.anchoranalysis.anchor.mpp.bean.regionmap.RegionMembership;
import org.anchoranalysis.anchor.mpp.mark.GlobalRegionIdentifiers;
import org.anchoranalysis.anchor.mpp.mark.Mark;
import org.anchoranalysis.anchor.mpp.proposer.PositionProposer;
import org.anchoranalysis.anchor.mpp.proposer.ProposerContext;
import org.anchoranalysis.anchor.mpp.pxlmark.PxlMark;
import org.anchoranalysis.anchor.mpp.pxlmark.memo.PxlMarkMemo;
import org.anchoranalysis.core.geometry.Point3d;
import org.anchoranalysis.core.random.RandomNumberGenerator;
import org.anchoranalysis.image.extent.BoundingBox;
import org.anchoranalysis.image.extent.Extent;

import lombok.RequiredArgsConstructor;


// Proposes a position from somewhere in a list of Memos, with some extra conditions
// NOT INTENDED TO BE IN A CONFIG FILE
@RequiredArgsConstructor
class PositionProposerMemoList implements PositionProposer {

	private final List<PxlMarkMemo> listPxlMarkMemo;
	private final Mark markBlock;
	
	private static Point3d randomPosition( BoundingBox bbox, RandomNumberGenerator re ) {
		
		Extent extent = bbox.extent();
		
		int x = bbox.cornerMin().getX() + (int) (re.nextDouble() * extent.getX() );
		int y = bbox.cornerMin().getY() + (int) (re.nextDouble() * extent.getY() );
		int z = bbox.cornerMin().getZ() + (int) (re.nextDouble() * extent.getZ() );
		
		return new Point3d(x,y,z);
	}

	@Override
	public Optional<Point3d> propose(ProposerContext context) {
		
		RegionMembership rm = context.getRegionMap().membershipForIndex(GlobalRegionIdentifiers.SUBMARK_INSIDE);
		
		byte flags = rm.flags();
		
		if (listPxlMarkMemo.isEmpty()) {
			return Optional.empty();
		}
		
		// ASSUMES a single channel
		
		int numTries = 20;

		Point3d pnt;
		
		int i = 0;
		while(true) {
		
			if (i++==numTries) {
				return Optional.empty();
			}
			
			// We keep randomly picking a memo from the list 
			// And randomly taking positions until we find a position that matches
			int pmmIndex = (int) (context.getRandomNumberGenerator().nextDouble() * listPxlMarkMemo.size());
			PxlMarkMemo pmm = listPxlMarkMemo.get(pmmIndex);
			PxlMark pm = pmm.doOperation();
			
			BoundingBox bbox = pm.getBoundingBox();
			pnt = randomPosition(bbox, context.getRandomNumberGenerator());
			
			int relX = (int) pnt.getX() - bbox.cornerMin().getX();
			int relY = (int) pnt.getY() - bbox.cornerMin().getY();
			int relZ = (int) pnt.getZ() - bbox.cornerMin().getZ();
			
			byte membershipExst = pm.getVoxelBox().getPixelsForPlane(relZ).get( bbox.extent().offset(relX, relY) );
			
			// If it's not inside our mark, then we don't consider it
			if (!rm.isMemberFlag(membershipExst, flags)) {
				continue;
			}
			
			byte membership = markBlock.evalPntInside(pnt);
			
			// If it's inside our block mark, then we don't consider it
			if (rm.isMemberFlag(membership, flags)) {
				continue;
			}
			
			break;
		}

		return Optional.of(pnt);
							
	}
}
