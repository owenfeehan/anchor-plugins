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

package org.anchoranalysis.plugin.image.feature.bean.object.single.shared;

import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.annotation.SkipInit;
import org.anchoranalysis.bean.xml.exception.ProvisionFailedException;
import org.anchoranalysis.core.exception.InitException;
import org.anchoranalysis.feature.calculate.FeatureCalculationException;
import org.anchoranalysis.feature.calculate.FeatureInitialization;
import org.anchoranalysis.feature.calculate.cache.ChildCacheName;
import org.anchoranalysis.feature.calculate.cache.SessionInput;
import org.anchoranalysis.image.bean.nonbean.init.ImageInitialization;
import org.anchoranalysis.image.bean.provider.MaskProvider;
import org.anchoranalysis.image.core.mask.Mask;
import org.anchoranalysis.image.feature.bean.object.pair.FeaturePairObjects;
import org.anchoranalysis.image.feature.bean.object.single.FeatureSingleObject;
import org.anchoranalysis.image.feature.input.FeatureInputSingleObject;

/**
 * Evaluates the object as a pair-feature together with the binary-mask from the shard objects.
 *
 * @author Owen Feehan
 */
public class PairedWithMask extends FeatureSingleObject {

    // START BEAN PROPERTIES
    @BeanField @Getter @Setter private FeaturePairObjects item;

    // This cannot be initialized in the normal way, as Feature isn't contained in a Shared-Objects
    // container. So instead it's initialized at a later point.
    @BeanField @SkipInit @Getter @Setter private MaskProvider mask;
    // END BEAN PROPERTIES

    private Mask createdMask;

    @Override
    protected void beforeCalc(FeatureInitialization initialization) throws InitException {
        super.beforeCalc(initialization);

        mask.initRecursive(
                new ImageInitialization(initialization.sharedObjectsRequired()), getLogger());

        try {
            createdMask = mask.get();
        } catch (ProvisionFailedException e) {
            throw new InitException(e);
        }
    }

    @Override
    public double calculate(SessionInput<FeatureInputSingleObject> input)
            throws FeatureCalculationException {
        return input.forChild()
                .calculate(
                        item,
                        new CalculatePairInput(createdMask),
                        new ChildCacheName(PairedWithMask.class, createdMask.hashCode()));
    }
}
