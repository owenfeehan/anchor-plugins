package ch.ethz.biol.cell.mpp.mark.proposer.orientation;

import org.anchoranalysis.anchor.mpp.bean.proposer.OrientationProposer;
import org.anchoranalysis.anchor.mpp.proposer.error.ErrorNode;

/*
 * #%L
 * anchor-plugin-mpp
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


import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.random.RandomNumberGenerator;
import org.anchoranalysis.image.extent.ImageDim;
import org.anchoranalysis.image.orientation.Orientation;
import org.anchoranalysis.image.orientation.Orientation2D;

import ch.ethz.biol.cell.imageprocessing.bound.BidirectionalBound;
import ch.ethz.biol.cell.mpp.bound.BoundCalculator;
import ch.ethz.biol.cell.mpp.mark.Mark;
import ch.ethz.biol.cell.mpp.mark.MarkAbstractPosition;

public class LongestExtent extends OrientationProposer {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5122961749674495354L;

	// START BEAN
	@BeanField
	private double incrementDegrees = 1;
	
	@BeanField
	private BoundCalculator boundCalculator;
	// END BEAN
	
	@Override
	public Orientation propose(Mark mark, ImageDim dim, RandomNumberGenerator re, ErrorNode proposerFailureDescription ) {
		
		MarkAbstractPosition markC = (MarkAbstractPosition) mark;
		
		double incrementRadians = (incrementDegrees / 180) * Math.PI;
		
		double maxExtent = -1;
		double angleAtMax = 0;
		
		// We loop through every positive angle and pick the one with the greatest extent
		for (double angle=0; angle < Math.PI; angle += incrementRadians) {
		
			BidirectionalBound bib = boundCalculator.calcBound( markC.getPos(), new Orientation2D( angle ).createRotationMatrix(), null);
			double max = bib.getMaxOfMax();
			
			if (max>maxExtent) {
				max = maxExtent;
				angleAtMax = angle;
			}
		}
		
		return new Orientation2D( angleAtMax );
	}

	public double getIncrementDegrees() {
		return incrementDegrees;
	}

	public void setIncrementDegrees(double incrementDegrees) {
		this.incrementDegrees = incrementDegrees;
	}

	@Override
	public boolean isCompatibleWith(Mark testMark) {
		return testMark instanceof MarkAbstractPosition;
	}

	public BoundCalculator getBoundCalculator() {
		return boundCalculator;
	}

	public void setBoundCalculator(BoundCalculator boundCalculator) {
		this.boundCalculator = boundCalculator;
	}
}
