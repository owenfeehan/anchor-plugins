package org.anchoranalysis.plugin.image.feature.bean.object.single.shape;

import java.util.ArrayList;

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


import java.util.List;

import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.geometry.Point3d;
import org.anchoranalysis.core.geometry.Point3i;
import org.anchoranalysis.feature.cache.SessionInput;
import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.image.feature.bean.object.single.FeatureSingleObject;
import org.anchoranalysis.image.feature.object.input.FeatureInputSingleObject;
import org.anchoranalysis.image.object.ObjectMask;
import org.anchoranalysis.image.outline.FindOutline;
import org.anchoranalysis.image.points.PointsFromBinaryVoxelBox;

import cern.colt.list.DoubleArrayList;
import cern.jet.stat.Descriptive;
import lombok.Getter;
import lombok.Setter;

// Standard deviation of distance from surface voxels to centroid
public class ObjectRadiusStandardDeviation extends FeatureSingleObject {

	// START BEAN PROPERTIES
	@BeanField @Getter @Setter
	private boolean cov = false;	// Returns the coefficient of variation (stdDev/mean) instead of stdDev
	// END BEAN PROPERTIES
	
	@Override
	public double calc(SessionInput<FeatureInputSingleObject> input) throws FeatureCalcException {
		
		ObjectMask om = input.get().getObjectMask();
		
		// Get the outline
		List<Point3i> pntsOutline = createMaskOutlineAsPoints(om, 1);

		// Distances from the center to each point on the outline
		DoubleArrayList distances = distancesToPoints(
			om.centerOfGravity(),
			pntsOutline
		);
		
		return calcStatistic(distances);
	}
	
	private static DoubleArrayList distancesToPoints( Point3d pntFrom, List<Point3i> pntsTo ) {
		DoubleArrayList distances = new DoubleArrayList( pntsTo.size() );
		for( Point3i p : pntsTo ) {
			
			Point3d pShift = new Point3d(
				p.getX() + 0.5,
				p.getY() + 0.5,
				p.getZ() + 0.5
			);
			
			double d = pntFrom.distance(pShift);
			distances.add(d);
		}
		return distances;
	}
	
	private double calcStatistic( DoubleArrayList distances ) {
		// Calculate Std Deviation
		int size = distances.size();
		double sum = Descriptive.sum( distances );
		
		double sumOfSquares = Descriptive.sumOfSquares(distances);
		double variance = Descriptive.variance( size, sum, sumOfSquares );
		double stdDev = Descriptive.standardDeviation( variance );
		
		if (cov) {
			double mean = sum/size;
			return stdDev/mean;
		} else {
			return stdDev;
		}
	}

	private static List<Point3i> createMaskOutlineAsPoints(ObjectMask mask, int numberErosions) {

		List<Point3i> ptsOutline = new ArrayList<>();
			
		ObjectMask outline = FindOutline.outline(mask, numberErosions, false, true);
		PointsFromBinaryVoxelBox.addPointsFromVoxelBox3D(
			outline.binaryVoxelBox(),
			outline.getBoundingBox().cornerMin(),
			ptsOutline
		);
		
		return ptsOutline;
	}
}
