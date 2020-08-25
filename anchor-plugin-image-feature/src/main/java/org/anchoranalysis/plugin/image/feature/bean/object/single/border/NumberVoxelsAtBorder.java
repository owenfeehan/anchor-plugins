/*-
 * #%L
 * anchor-plugin-image-feature
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

package org.anchoranalysis.plugin.image.feature.bean.object.single.border;

import org.anchoranalysis.core.functional.function.CheckedSupplier;
import org.anchoranalysis.feature.calculate.FeatureCalculationException;
import org.anchoranalysis.feature.energy.EnergyStack;
import org.anchoranalysis.image.object.ObjectMask;
import org.anchoranalysis.image.voxel.kernel.ApplyKernel;
import org.anchoranalysis.image.voxel.kernel.outline.OutlineKernel3;
import org.anchoranalysis.image.voxel.kernel.outline.OutlineKernelParameters;
import org.anchoranalysis.plugin.image.feature.bean.object.single.OutlineKernelBase;

public class NumberVoxelsAtBorder extends OutlineKernelBase {
    
    @Override
    protected double calculateWithParameters(ObjectMask object, OutlineKernelParameters parameters, CheckedSupplier<EnergyStack,FeatureCalculationException> energyStack)
            throws FeatureCalculationException {
        return (double) numberBorderPixels(object, parameters);
    }

    public static int numberBorderPixels(
            ObjectMask object,
            OutlineKernelParameters parameters) {
        OutlineKernel3 kernel =
                new OutlineKernel3(
                        object.binaryValuesByte(), parameters);
        return ApplyKernel.applyForCount(kernel, object.voxels());
    }
  
}
