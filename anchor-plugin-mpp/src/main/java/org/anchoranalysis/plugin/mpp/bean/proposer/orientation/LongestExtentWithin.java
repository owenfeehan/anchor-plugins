package org.anchoranalysis.plugin.mpp.bean.proposer.orientation;

/*-
 * #%L
 * anchor-plugin-mpp
 * %%
 * Copyright (C) 2010 - 2020 Owen Feehan
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

import java.util.Optional;

import org.anchoranalysis.anchor.mpp.bean.bound.BoundCalculator;
import org.anchoranalysis.anchor.mpp.bean.bound.RslvdBound;
import org.anchoranalysis.anchor.mpp.bean.proposer.OrientationProposer;
import org.anchoranalysis.anchor.mpp.mark.Mark;
import org.anchoranalysis.anchor.mpp.mark.MarkAbstractPosition;
import org.anchoranalysis.anchor.mpp.proposer.ProposalAbnormalFailureException;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.name.provider.NamedProviderGetException;
import org.anchoranalysis.core.random.RandomNumberGenerator;
import org.anchoranalysis.image.extent.ImageDimensions;
import org.anchoranalysis.image.orientation.Orientation;
import org.anchoranalysis.image.orientation.Orientation2D;
import org.anchoranalysis.image.orientation.Orientation3DEulerAngles;

// Gets the longest extent within a certain ratio between the bounds,
//   and below the upper maximum
public class LongestExtentWithin extends OrientationProposer {

	// START BEAN
	@BeanField
	private double incrementDegrees = 1;
	
	@BeanField
	private double boundsRatio = 1.1;
	
	@BeanField
	private BoundCalculator boundCalculator;

	@BeanField
	private boolean rotateOnlyIn2DPlane = false;
	// END BEAN
	
	@Override
	public Optional<Orientation> propose(Mark mark, ImageDimensions dim, RandomNumberGenerator re ) throws ProposalAbnormalFailureException {
		
		try {
			RslvdBound minMaxBound = getInitializationParameters().getMarkBounds().calcMinMax(dim.getRes(), dim.getZ()>1 );
			return findAllOrientations(mark, minMaxBound, dim).sample(re);
			
		} catch (NamedProviderGetException e) {
			throw new ProposalAbnormalFailureException(e.summarize());
		}
	}

	@Override
	public boolean isCompatibleWith(Mark testMark) {
		return testMark instanceof MarkAbstractPosition;
	}
	
	private OrientationList findAllOrientations2D(Mark mark, RslvdBound minMaxBound) throws ProposalAbnormalFailureException {
		
		double incrementRadians = (incrementDegrees / 180) * Math.PI;
		
		OrientationList listOrientations = new OrientationList(boundCalculator, boundsRatio);
		
		// We loop through every positive angle and pick the one with the greatest extent
		for (double angle=0; angle < Math.PI; angle += incrementRadians) {
		
			Orientation orientation = new Orientation2D(angle);
			
			listOrientations.addOrientationIfUseful(orientation, mark, minMaxBound);

		}
		
		return listOrientations;
	}
	
	
	private OrientationList findAllOrientations3D(Mark mark, RslvdBound minMaxBound) throws ProposalAbnormalFailureException {
		
		double incrementRadians = (incrementDegrees / 180) * Math.PI;
		
		OrientationList listOrientations = new OrientationList(boundCalculator, boundsRatio);
		
		// We loop through every positive angle and pick the one with the greatest extent
		for (double x=0; x < Math.PI; x += incrementRadians) {
			for (double y=0; y < Math.PI; y += incrementRadians) {
				for (double z=0; z < Math.PI; z += incrementRadians) {
					
					listOrientations.addOrientationIfUseful(
						new Orientation3DEulerAngles(x,y,z),
						mark,
						minMaxBound
					);
				}
			}
		}
		
		return listOrientations;
	}
	
	
	private OrientationList findAllOrientations(Mark mark, RslvdBound minMaxBound, ImageDimensions dim) throws ProposalAbnormalFailureException {
		
		if (dim.getZ()>1 && !rotateOnlyIn2DPlane) {
			return findAllOrientations3D(mark, minMaxBound);
		} else {
			return findAllOrientations2D(mark, minMaxBound);
		}
	}
	
	public double getIncrementDegrees() {
		return incrementDegrees;
	}

	public void setIncrementDegrees(double incrementDegrees) {
		this.incrementDegrees = incrementDegrees;
	}

	public BoundCalculator getBoundCalculator() {
		return boundCalculator;
	}

	public void setBoundCalculator(BoundCalculator boundCalculator) {
		this.boundCalculator = boundCalculator;
	}

	public double getBoundsRatio() {
		return boundsRatio;
	}

	public void setBoundsRatio(double boundsRatio) {
		this.boundsRatio = boundsRatio;
	}

	public boolean isRotateOnlyIn2DPlane() {
		return rotateOnlyIn2DPlane;
	}

	public void setRotateOnlyIn2DPlane(boolean rotateOnlyIn2DPlane) {
		this.rotateOnlyIn2DPlane = rotateOnlyIn2DPlane;
	}
}
