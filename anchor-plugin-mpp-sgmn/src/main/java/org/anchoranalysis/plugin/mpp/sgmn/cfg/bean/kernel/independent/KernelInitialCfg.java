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
import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.anchor.mpp.bean.proposer.CfgProposer;
import org.anchoranalysis.anchor.mpp.cfg.Cfg;
import org.anchoranalysis.anchor.mpp.feature.mark.ListUpdatableMarkSetCollection;
import org.anchoranalysis.anchor.mpp.mark.Mark;
import org.anchoranalysis.anchor.mpp.mark.set.UpdateMarkSetException;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.image.extent.Dimensions;
import org.anchoranalysis.mpp.sgmn.bean.kernel.KernelIndependent;
import org.anchoranalysis.mpp.sgmn.kernel.KernelCalculationContext;
import org.anchoranalysis.mpp.sgmn.kernel.KernelCalcNRGException;

public class KernelInitialCfg extends KernelIndependent<Cfg> {

    // START BEAN LIST
    @BeanField @Getter @Setter private CfgProposer cfgProposer;
    // END BEAN LIST

    private Optional<Cfg> lastCfg;

    @Override
    public boolean isCompatibleWith(Mark testMark) {
        return cfgProposer.isCompatibleWith(testMark);
    }

    @Override
    public Optional<Cfg> makeProposal(Optional<Cfg> existing, KernelCalculationContext context)
            throws KernelCalcNRGException {
        this.lastCfg = InitCfgUtilities.propose(cfgProposer, context);
        return lastCfg;
    }

    @Override
    public double calculateAcceptanceProbability(
            int existingSize,
            int proposalSize,
            double poissonIntensity,
            Dimensions dimensions,
            double densityRatio) {
        // We always accept
        return 1;
    }

    @Override
    public String describeLast() {
        return String.format("initialCfg(size=%d)", this.lastCfg.map(Cfg::size).orElse(-1));
    }

    @Override
    public void updateAfterAcceptance(
            ListUpdatableMarkSetCollection updatableMarkSetCollection, Cfg exst, Cfg accptd)
            throws UpdateMarkSetException {
        // NOTHING TO DO
    }

    @Override
    public int[] changedMarkIDArray() {
        return this.lastCfg.map(Cfg::createIdArr).orElse(new int[] {});
    }
}
