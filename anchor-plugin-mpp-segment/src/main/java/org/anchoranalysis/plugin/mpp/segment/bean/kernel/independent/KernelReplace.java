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

package org.anchoranalysis.plugin.mpp.segment.bean.kernel.independent;

import java.util.Optional;
import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.functional.OptionalUtilities;
import org.anchoranalysis.image.core.dimensions.Dimensions;
import org.anchoranalysis.mpp.feature.mark.UpdatableMarksList;
import org.anchoranalysis.mpp.mark.UpdateMarkSetException;
import org.anchoranalysis.mpp.segment.bean.optimization.kernel.Kernel;
import org.anchoranalysis.mpp.segment.bean.optimization.kernel.KernelPosNeg;
import org.anchoranalysis.mpp.segment.optimization.kernel.KernelCalculateEnergyException;
import org.anchoranalysis.mpp.segment.optimization.kernel.KernelCalculationContext;
import org.apache.commons.lang.ArrayUtils;

public abstract class KernelReplace<T> extends KernelPosNeg<T, UpdatableMarksList> {

    private KernelBirth<T> kernelBirth;
    private KernelDeath<T> kernelDeath;

    private Optional<T> afterDeathProp;

    // START BEAN PROPERTIES
    @BeanField @Getter @Setter private int birthRepeats = 1;
    // END BEAN PROPERTIES

    private boolean hasBeenInit = false;

    /** Must be called before makeProposal */
    protected void init(KernelBirth<T> kernelBirth, KernelDeath<T> kernelDeath) {
        this.kernelBirth = kernelBirth;
        this.kernelDeath = kernelDeath;
        assert (kernelDeath != null);
        hasBeenInit = true;
    }

    @Override
    public Optional<T> makeProposal(Optional<T> existing, KernelCalculationContext context)
            throws KernelCalculateEnergyException {

        if (existing.isPresent()) {
            return Optional.empty();
        }

        afterDeathProp = kernelDeath.makeProposal(existing, context);
        return OptionalUtilities.flatMap(
                afterDeathProp, prop -> kernelBirth.makeProposal(Optional.of(prop), context));
    }

    @Override
    public double calculateAcceptanceProbability(
            int existingSize,
            int proposalSize,
            double poissonIntens,
            Dimensions dimensions,
            double densityRatio) {
        return Math.min(densityRatio, 1.0);
    }

    @Override
    public String describeLast() {
        assert (hasBeenInit);
        assert (kernelDeath != null);
        return String.format(
                "replace_%d(%s->%s)", birthRepeats, changedID(kernelDeath), changedID(kernelBirth));
    }

    @Override
    public void updateAfterAcceptance(
            UpdatableMarksList updatableState, T energyExisting, T energyNew)
            throws UpdateMarkSetException {

        OptionalUtilities.ifPresent(
                afterDeathProp,
                prop -> {
                    kernelDeath.updateAfterAcceptance(updatableState, energyExisting, prop);
                    kernelBirth.updateAfterAcceptance(updatableState, prop, energyNew);
                });
    }

    @Override
    public int[] changedMarkIDArray() {
        return makeArray(kernelBirth.changedMarkIDArray(), kernelDeath.changedMarkIDArray());
    }

    private static <S, T> String changedID(Kernel<S, T> kernel) {
        int[] arr = kernel.changedMarkIDArray();

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < arr.length; i++) {
            if (i != 0) {
                sb.append(", ");
            }
            sb.append(arr[i]);
        }
        return sb.toString();
    }

    private static int[] makeArray(int[] first, int[] second) {

        if (first != null) {

            if (second != null) {
                return ArrayUtils.addAll(first, second);
            } else {
                return first;
            }

        } else {

            if (second != null) {
                return second;
            } else {
                return new int[] {};
            }
        }
    }

    public boolean hasBeenInit() {
        return hasBeenInit;
    }
}
