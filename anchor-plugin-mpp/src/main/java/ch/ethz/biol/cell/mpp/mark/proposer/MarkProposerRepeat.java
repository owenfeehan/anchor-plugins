package ch.ethz.biol.cell.mpp.mark.proposer;

import org.anchoranalysis.anchor.mpp.bean.proposer.MarkProposer;
import org.anchoranalysis.anchor.mpp.proposer.ProposerContext;
import org.anchoranalysis.anchor.mpp.proposer.visualization.ICreateProposalVisualization;

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

import anchor.provider.bean.ProposalAbnormalFailureException;
import ch.ethz.biol.cell.mpp.mark.Mark;
import ch.ethz.biol.cell.mpp.mark.pxlmark.memo.PxlMarkMemo;

// Repeat multiple times until we get a successful proposal
//  abandoning after maxIter is reached
public class MarkProposerRepeat extends MarkProposer {

	/**
	 * 
	 */
	private static final long serialVersionUID = 286045681411030790L;
	
	// START BEAN PROPERTIES
	@BeanField
	private MarkProposer item;
	
	@BeanField
	private int maxIter = 20;
	// END BEAN PROPERTIES	

	@Override
	public boolean isCompatibleWith(Mark testMark) {
		return item.isCompatibleWith(testMark);
	}

	@Override
	public boolean propose(PxlMarkMemo inputMark, ProposerContext context) throws ProposalAbnormalFailureException {

		for (int i=0; i<maxIter; i++) {
			
			if (item.propose(inputMark, context)) {
				return true;
			}
			
		}
		
		context.getErrorNode().add("max number of iterations reached");
		
		return false;
	}

	public MarkProposer getItem() {
		return item;
	}

	public void setItem(MarkProposer item) {
		this.item = item;
	}

	public int getMaxIter() {
		return maxIter;
	}

	public void setMaxIter(int maxIter) {
		this.maxIter = maxIter;
	}

	@Override
	public ICreateProposalVisualization proposalVisualization(boolean detailed) {
		return item.proposalVisualization(detailed);
	}

}
