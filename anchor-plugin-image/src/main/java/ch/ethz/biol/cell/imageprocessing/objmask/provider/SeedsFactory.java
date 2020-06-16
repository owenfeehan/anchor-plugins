package ch.ethz.biol.cell.imageprocessing.objmask.provider;



/*
 * #%L
 * anchor-image
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


import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.geometry.ReadableTuple3i;
import org.anchoranalysis.image.extent.BoundingBox;
import org.anchoranalysis.image.extent.ImageDim;
import org.anchoranalysis.image.objectmask.ObjectMask;
import org.anchoranalysis.image.objectmask.ObjectMaskCollection;
import org.anchoranalysis.image.seed.SeedCollection;
import org.anchoranalysis.image.seed.SeedObjMask;

class SeedsFactory {
	
	public static SeedCollection createSeedsWithoutMask( ObjectMaskCollection seeds ) {
		// We create a collection of seeds localised appropriately
		// NB: we simply change the object seeds, as it seemingly won't be used again!!!
		SeedCollection seedsObj = new SeedCollection();
		for( ObjectMask om : seeds ) {
			seedsObj.add(
				createSeed(om)
			);
		}
				
		return seedsObj;
	}
	
	public static SeedCollection createSeedsWithMask(
		ObjectMaskCollection seeds,
		ObjectMask containingMask,
		ReadableTuple3i subtractFromCrnrMin,
		ImageDim dim
	) throws CreateException {
		// We create a collection of seeds localised appropriately
		// NB: we simply change the object seeds, as it seemingly won't be used again!!!
		SeedCollection seedsObj = new SeedCollection();
		for( ObjectMask om : seeds ) {
			seedsObj.add(
				createSeedWithinMask(
					om,
					containingMask.getBoundingBox(),
					subtractFromCrnrMin,
					dim
				)
			);
		}
		
		assert(
			seedsObj.verifySeedsAreInside( containingMask.getBoundingBox().extent())
		);
		return seedsObj;
	}
	
	private static SeedObjMask createSeed( ObjectMask om ) {
		return new SeedObjMask(
			om.duplicate()
		);
	}
	
	private static SeedObjMask createSeedWithinMask(
		ObjectMask om,
		BoundingBox containingBBox,
		ReadableTuple3i subtractFromCrnrMin,
		ImageDim dim
	) throws CreateException {
		ObjectMask omSeedDup = om.duplicate();
		omSeedDup.shiftBackBy(subtractFromCrnrMin);
		
		// If a seed object is partially located outside an object, the above line might fail, so we should test
		if (!containingBBox.contains().box( omSeedDup.getBoundingBox())) {
			
			// We only take the part of the seed object that intersects with our bbox
			BoundingBox bboxIntersect = containingBBox.intersection().withInside( omSeedDup.getBoundingBox(), dim.getExtnt() ).orElseThrow( ()->
				new CreateException("No bounding box intersection exists between seed and containing bounding-box")
			);
			omSeedDup = omSeedDup.createSubmaskAlwaysNew(bboxIntersect);
		}
		return new SeedObjMask(omSeedDup);
	}
}
