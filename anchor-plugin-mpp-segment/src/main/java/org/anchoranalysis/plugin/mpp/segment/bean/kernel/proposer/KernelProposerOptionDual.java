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

package org.anchoranalysis.plugin.mpp.segment.bean.kernel.proposer;

import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.annotation.NonNegative;
import org.anchoranalysis.mpp.segment.bean.optimization.kernel.KernelPosNeg;
import org.anchoranalysis.mpp.segment.bean.optimization.kernel.KernelProposerOption;
import org.anchoranalysis.mpp.segment.optimization.kernel.WeightedKernel;

public class KernelProposerOptionDual<T, S> extends KernelProposerOption<T, S> {

    // START BEAN PROPERTIES
    @BeanField @Getter @Setter private KernelPosNeg<T, S> kernelPositive;

    @BeanField @Getter @Setter private KernelPosNeg<T, S> kernelNegative;

    @BeanField @NonNegative @Getter @Setter private double weightPositive = -1;

    @BeanField @NonNegative @Getter @Setter private double weightNegative = -1;
    // END BEAN PROPERTIES

    @Override
    // Add weighted kernel factories to a list, and returns the total weight
    public double addWeightedKernelFactories(List<WeightedKernel<T, S>> list) {

        kernelPositive.setProbPos(weightPositive);
        kernelPositive.setProbNeg(weightNegative);

        kernelNegative.setProbPos(weightNegative);
        kernelNegative.setProbNeg(weightPositive);

        list.add(new WeightedKernel<>(kernelPositive, weightPositive));
        list.add(new WeightedKernel<>(kernelNegative, weightNegative));
        return getWeightPositive() + getWeightNegative();
    }
}
