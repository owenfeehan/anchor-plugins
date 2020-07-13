package org.anchoranalysis.plugin.image.feature.bean.object.pair.touching;

import java.util.Optional;

/*
 * #%L
 * anchor-plugin-image-feature
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


import org.anchoranalysis.feature.cache.calculation.FeatureCalculation;
import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.image.extent.BoundingBox;
import org.anchoranalysis.image.extent.Extent;
import org.anchoranalysis.image.feature.object.input.FeatureInputPairObjects;
import org.anchoranalysis.image.object.ObjectMask;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;

@AllArgsConstructor @EqualsAndHashCode(callSuper=false)
class CalculateIntersectionOfDilatedBoundingBox extends FeatureCalculation<Optional<BoundingBox>, FeatureInputPairObjects> {
	
	private final boolean do3D;

	@Override
	protected Optional<BoundingBox> execute(FeatureInputPairObjects input) throws FeatureCalcException {
		return findIntersectionOfDilatedBoundingBox(
			input.getFirst(),
			input.getSecond(),
			input.getDimensionsRequired().getExtent()
		);
	}

	private Optional<BoundingBox> findIntersectionOfDilatedBoundingBox( ObjectMask om1, ObjectMask om2, Extent extent) {
		
		// Grow each bounding box
		BoundingBox bbox1 = dilatedBoundingBoxFor(om1, extent);
		BoundingBox bbox2 = dilatedBoundingBoxFor(om2, extent);
		
		// Find the intersection
		return bbox1.intersection().withInside(bbox2, extent);
	}
	
	private BoundingBox dilatedBoundingBoxFor( ObjectMask om, Extent extent ) {
		return om.getVoxelBoxBounded().dilate(
			do3D,
			Optional.of(extent)
		);
	}
}