package org.anchoranalysis.plugin.mpp.sgmn.cfg.optscheme;

import java.util.Optional;

/*-
 * #%L
 * anchor-plugin-mpp-sgmn
 * %%
 * Copyright (C) 2010 - 2019 Owen Feehan, ETH Zurich, University of Zurich, Hoffmann la Roche
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

import java.util.function.ToDoubleFunction;

import org.anchoranalysis.anchor.mpp.bean.anneal.AnnealScheme;
import org.anchoranalysis.mpp.sgmn.bean.kernel.Kernel;
import org.anchoranalysis.mpp.sgmn.kernel.KernelCalcContext;
import org.anchoranalysis.mpp.sgmn.optscheme.ExtractScoreSize;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class AccptProbCalculator<T> {

	// START REQUIRED ARGUMENTS
	private final AnnealScheme annealScheme;
	private final ExtractScoreSize<T> extracter;
	// END REQUIRED ARGUMENTS
		
	public double calcAccptProb( Kernel<?> kernel, Optional<T> crnt, T proposal, int iter, KernelCalcContext context ) {
		return kernel.calcAccptProb(
			sizeOrZero(crnt),
			sizeOrZero(Optional.of(proposal)),
			context.cfgGen().getCfgGen().getReferencePoissonIntensity(),
			context.proposer().getDimensions(),
			calcDensityRatio(crnt, Optional.of(proposal), iter)
		);
	}

	public ToDoubleFunction<T> getFuncScore() {
		return extracter::extractScore;
	}
	
	private int sizeOrZero( Optional<T> crnt ) {
		return crnt.map(extracter::extractSize).orElse(0);
	}

	private double calcDensityRatio( Optional<T> crnt, Optional<T> proposal, int iter ) {
		
		if (!proposal.isPresent() || !crnt.isPresent()) {
			return Double.NaN;
		}
		
		return annealScheme.calcDensityRatio(
			extracter.extractScore(proposal.get()),
			extracter.extractScore(crnt.get()),
			iter
		);				
	}

}
