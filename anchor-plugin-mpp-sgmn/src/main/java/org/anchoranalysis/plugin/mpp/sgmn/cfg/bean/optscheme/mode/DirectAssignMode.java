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

package org.anchoranalysis.plugin.mpp.sgmn.cfg.bean.optscheme.mode;

import org.anchoranalysis.anchor.mpp.bean.anneal.AnnealScheme;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.mpp.sgmn.optscheme.ExtractScoreSize;
import org.anchoranalysis.mpp.sgmn.optscheme.StateReporter;
import org.anchoranalysis.plugin.mpp.sgmn.cfg.bean.optscheme.kernelbridge.KernelStateBridge;
import org.anchoranalysis.plugin.mpp.sgmn.cfg.bean.optscheme.kernelbridge.KernelStateBridgeIdentity;
import org.anchoranalysis.plugin.mpp.sgmn.cfg.bean.optscheme.statereporter.StateReporterIdentity;
import org.anchoranalysis.plugin.mpp.sgmn.cfg.optscheme.AccptProbCalculator;

/**
 * Directly assigns a CfgNRG for every optimisation step
 *
 * @param S state-type for optimization
 */
public class DirectAssignMode<S> extends AssignMode<S, S, S> {

    // START BEAN PROPERTIES
    @BeanField private ExtractScoreSize<S> extractScoreSize;
    // END BEAN PROPERTIES

    public DirectAssignMode() {
        // Standard bean constructor
    }

    public DirectAssignMode(ExtractScoreSize<S> extractScoreSize) {
        this.extractScoreSize = extractScoreSize;
    }

    public AccptProbCalculator<S> probCalculator(AnnealScheme annealScheme) {
        return new AccptProbCalculator<>(annealScheme, extractScoreSize);
    }

    @Override
    public KernelStateBridge<S, S> kernelStateBridge() {
        return new KernelStateBridgeIdentity<>();
    }

    @Override
    public StateReporter<S, S> stateReporter() {
        return new StateReporterIdentity<>();
    }

    @Override
    public ExtractScoreSize<S> extractScoreSizeReport() {
        return extractScoreSize;
    }

    @Override
    public ExtractScoreSize<S> extractScoreSizeState() {
        return extractScoreSizeReport();
    }

    public ExtractScoreSize<S> getExtractScoreSize() {
        return extractScoreSize;
    }

    public void setExtractScoreSize(ExtractScoreSize<S> extractScoreSize) {
        this.extractScoreSize = extractScoreSize;
    }
}
