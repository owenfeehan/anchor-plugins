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

package org.anchoranalysis.plugin.mpp.segment.bean.optimization.mode;

import org.anchoranalysis.bean.AnchorBean;
import org.anchoranalysis.mpp.bean.anneal.AnnealScheme;
import org.anchoranalysis.mpp.feature.mark.UpdatableMarksList;
import org.anchoranalysis.mpp.segment.bean.optimization.ExtractScoreSize;
import org.anchoranalysis.mpp.segment.bean.optimization.StateReporter;
import org.anchoranalysis.mpp.segment.kernel.KernelAssigner;
import org.anchoranalysis.plugin.mpp.segment.bean.optimization.kernelbridge.KernelStateBridge;
import org.anchoranalysis.plugin.mpp.segment.kernel.assigner.KernelAssignerCalculateEnergyFromKernel;
import org.anchoranalysis.plugin.mpp.segment.optimization.AccptProbCalculator;

/**
 * How assignments and other decisions are made in the SimulatedAnnealing optimizaton
 *
 * @author Owen Feehan
 * @param <S> reporting back type
 * @param <T> state-type for optimization
 * @param <U> target-type for kernel assignment
 */
public abstract class AssignMode<S, T, U> extends AnchorBean<AssignMode<S, T, U>> {

    public abstract AccptProbCalculator<T> probCalculator(AnnealScheme annealScheme);

    public KernelAssigner<U, T, UpdatableMarksList> kernelAssigner() {
        return new KernelAssignerCalculateEnergyFromKernel<>(kernelStateBridge());
    }

    public abstract KernelStateBridge<U, T> kernelStateBridge();

    public abstract StateReporter<T, S> stateReporter();

    public abstract ExtractScoreSize<S> extractScoreSizeReport();

    public abstract ExtractScoreSize<T> extractScoreSizeState();
}
