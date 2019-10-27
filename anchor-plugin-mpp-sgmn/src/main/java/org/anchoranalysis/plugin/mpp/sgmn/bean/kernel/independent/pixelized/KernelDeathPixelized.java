package org.anchoranalysis.plugin.mpp.sgmn.bean.kernel.independent.pixelized;

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

import org.anchoranalysis.anchor.mpp.proposer.ProposerContext;
import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.mpp.sgmn.kernel.KernelCalcNRGException;
import org.anchoranalysis.plugin.mpp.sgmn.bean.kernel.independent.KernelDeath;
import ch.ethz.biol.cell.mpp.mark.Mark;
import ch.ethz.biol.cell.mpp.nrg.CfgNRGPixelized;
import ch.ethz.biol.cell.mpp.pair.ListUpdatableMarkSetCollection;
import ch.ethz.biol.cell.mpp.pair.UpdateMarkSetException;

public class KernelDeathPixelized extends KernelDeath<CfgNRGPixelized> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Override
	protected MarkAnd<Mark, CfgNRGPixelized> removeAndUpdateNrg(CfgNRGPixelized exst, ProposerContext context) throws KernelCalcNRGException {
		return removeMarkAndUpdateNrg( exst, context );
	}

	@Override
	public void updateAfterAccpt(ListUpdatableMarkSetCollection updatableMarkSetCollection, CfgNRGPixelized nrgExst,
			CfgNRGPixelized nrgNew) throws UpdateMarkSetException {
		nrgExst.rmvFromUpdatablePairList(updatableMarkSetCollection, getMarkRmv() );
	}
		
	private static MarkAnd<Mark,CfgNRGPixelized> removeMarkAndUpdateNrg( CfgNRGPixelized exst, ProposerContext propContext ) throws KernelCalcNRGException {

		int index = selectIndexToRmv( exst.getCfg(), propContext );
		
		if (index==-1) {
			return new MarkAnd<>(null,null);
		}
		
		return new MarkAnd<>(
			exst.getMemoForIndex(index).getMark(),
			calcUpdatedNRGAfterRemoval(index, exst, propContext)
		);
	}
	
	private static CfgNRGPixelized calcUpdatedNRGAfterRemoval(
		int index,
		CfgNRGPixelized exst,
		ProposerContext propContext
	) throws KernelCalcNRGException {
		// We calculate a new NRG by exchanging our marks
		CfgNRGPixelized newNRG = exst.shallowCopy();
				
		try {
			newNRG.rmv( index, propContext.getNrgStack().getNrgStack() );
		} catch ( FeatureCalcException e ) {
			throw new KernelCalcNRGException( String.format("Cannot remove index %d", index), e);
		}
					
		return newNRG;		
	}
}
