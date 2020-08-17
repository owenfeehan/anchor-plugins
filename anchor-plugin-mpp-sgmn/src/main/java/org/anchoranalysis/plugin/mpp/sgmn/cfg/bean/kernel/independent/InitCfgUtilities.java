/*-
 * #%L
 * anchor-plugin-mpp-sgmn
 * %%
 * Copyright (C) 2010 - 2020 Owen Feehan, ETH Zurich, University of Zurich, Hoffmann-La Roche
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

package org.anchoranalysis.plugin.mpp.sgmn.cfg.bean.kernel.independent;

import java.util.Optional;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.anchoranalysis.anchor.mpp.bean.cfg.CfgGen;
import org.anchoranalysis.anchor.mpp.bean.proposer.CfgProposer;
import org.anchoranalysis.anchor.mpp.cfg.Cfg;
import org.anchoranalysis.anchor.mpp.proposer.ProposalAbnormalFailureException;
import org.anchoranalysis.anchor.mpp.proposer.ProposerContext;
import org.anchoranalysis.mpp.sgmn.kernel.KernelCalculationContext;
import org.anchoranalysis.mpp.sgmn.kernel.KernelCalcNRGException;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
class InitCfgUtilities {

    public static Optional<Cfg> propose(CfgProposer cfgProposer, KernelCalculationContext context)
            throws KernelCalcNRGException {
        ProposerContext propContext = context.proposer();

        // We don't expect an existing exsting CfgNRG, but rather null (or whatever)

        // Initial cfg
        return proposeCfg(context.cfgGen().getCfgGen(), cfgProposer, propContext);
    }

    private static Optional<Cfg> proposeCfg(
            CfgGen cfgGen, CfgProposer cfgProposer, ProposerContext propContext)
            throws KernelCalcNRGException {
        try {
            return cfgProposer.propose(cfgGen, propContext);
        } catch (ProposalAbnormalFailureException e) {
            throw new KernelCalcNRGException(
                    "Failed to propose an initial-cfg due to an abnormal error", e);
        }
    }
}
