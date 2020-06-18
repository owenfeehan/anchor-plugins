package org.anchoranalysis.plugin.mpp.bean.proposer.radii;

import java.util.Optional;

import org.anchoranalysis.anchor.mpp.bean.proposer.radii.RadiiProposer;
import org.anchoranalysis.anchor.mpp.mark.Mark;
import org.anchoranalysis.anchor.mpp.proposer.ProposalAbnormalFailureException;


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
import org.anchoranalysis.core.geometry.Point3d;
import org.anchoranalysis.core.random.RandomNumberGenerator;
import org.anchoranalysis.image.extent.ImageDim;
import org.anchoranalysis.image.orientation.Orientation;

public class RadiiProposerRepeat extends RadiiProposer {

	// START BEAN PROPERTIES
	@BeanField
	private RadiiProposer radiiProposer;
	
	@BeanField
	private int maxIter = 20;
	// END BEAN PROPERTIES

	@Override
	public Optional<Point3d> propose(
		Point3d pos,
		RandomNumberGenerator randomNumberGenerator,
		ImageDim dim,
		Orientation orientation
	) throws ProposalAbnormalFailureException {
		
		for (int i=0; i<maxIter; i++) {
			Optional<Point3d> point = radiiProposer.propose(
				pos,
				randomNumberGenerator,
				dim,
				orientation
			);
			if (point.isPresent()) {
				return point;
			}
		}
		
		return Optional.empty();
	}

	@Override
	public boolean isCompatibleWith(Mark testMark) {
		return radiiProposer.isCompatibleWith(testMark);
	}
	
	public RadiiProposer getRadiiProposer() {
		return radiiProposer;
	}

	public void setRadiiProposer(RadiiProposer radiiProposer) {
		this.radiiProposer = radiiProposer;
	}

	public int getMaxIter() {
		return maxIter;
	}

	public void setMaxIter(int maxIter) {
		this.maxIter = maxIter;
	}
}
