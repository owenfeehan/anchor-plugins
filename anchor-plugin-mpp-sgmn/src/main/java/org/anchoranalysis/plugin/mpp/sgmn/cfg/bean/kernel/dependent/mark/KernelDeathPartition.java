package org.anchoranalysis.plugin.mpp.sgmn.cfg.bean.kernel.dependent.mark;

import java.util.Optional;

import org.anchoranalysis.anchor.mpp.cfg.Cfg;
import org.anchoranalysis.anchor.mpp.feature.mark.ListUpdatableMarkSetCollection;
import org.anchoranalysis.anchor.mpp.mark.Mark;
import org.anchoranalysis.anchor.mpp.mark.set.UpdateMarkSetException;

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
import org.anchoranalysis.core.functional.OptionalUtilities;
import org.anchoranalysis.mpp.sgmn.kernel.KernelCalcNRGException;
import org.anchoranalysis.plugin.mpp.sgmn.cfg.bean.kernel.independent.KernelDeath;
import org.anchoranalysis.plugin.mpp.sgmn.cfg.optscheme.CfgFromPartition;

public class KernelDeathPartition extends KernelDeath<CfgFromPartition> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Override
	protected Optional<MarkAnd<Mark, CfgFromPartition>> removeAndUpdateNrg(CfgFromPartition exst, ProposerContext context)
			throws KernelCalcNRGException {

		int index = selectIndexToRmv( exst.getCfg(), context );
		
		if (index==-1) {
			return Optional.empty();
		}
		
		Cfg cfgNew = exst.getCfg().shallowCopy();
		cfgNew.remove(index);
		return Optional.of(
			new MarkAnd<>(
				exst.getCfg().get(index),
				exst.copyChange(cfgNew)
			)
		);
	}

	@Override
	public void updateAfterAccpt(ListUpdatableMarkSetCollection updatableMarkSetCollection, CfgFromPartition nrgExst,
			CfgFromPartition nrgNew) throws UpdateMarkSetException {
		OptionalUtilities.ifPresent(
			getMarkRmv(),
			nrgNew.getPartition()::moveAcceptedToAvailable
		);
	}
}
