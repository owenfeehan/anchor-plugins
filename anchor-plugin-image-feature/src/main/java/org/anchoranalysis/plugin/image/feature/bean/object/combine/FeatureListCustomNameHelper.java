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

package org.anchoranalysis.plugin.image.feature.bean.object.combine;

import java.util.List;
import org.anchoranalysis.bean.NamedBean;
import org.anchoranalysis.bean.exception.BeanDuplicateException;
import org.anchoranalysis.bean.xml.exception.ProvisionFailedException;
import org.anchoranalysis.core.exception.OperationFailedException;
import org.anchoranalysis.feature.bean.list.FeatureList;
import org.anchoranalysis.feature.bean.list.FeatureListFactory;
import org.anchoranalysis.feature.bean.list.FeatureListProvider;
import org.anchoranalysis.feature.input.FeatureInput;
import org.anchoranalysis.feature.store.NamedFeatureStore;
import org.anchoranalysis.feature.store.NamedFeatureStoreFactory;

/**
 * Duplicates and sets a custom-name on a list of features.
 *
 * @author Owen Feehan
 */
class FeatureListCustomNameHelper {

    private final NamedFeatureStoreFactory storeFactory;

    public FeatureListCustomNameHelper(NamedFeatureStoreFactory storeFactory) {
        this.storeFactory = storeFactory;
    }

    /**
     * Duplicates features and sets a custom-name based upon the named-bean
     *
     * @param <T> feature input-type
     * @param features the list of named-beans providing features
     * @return a simple feature-list of duplicated beans with derived custom-names set
     * @throws OperationFailedException
     */
    public <T extends FeatureInput> FeatureList<T> copyFeaturesCreateCustomName(
            List<NamedBean<FeatureListProvider<T>>> features) throws OperationFailedException {
        try {
            NamedFeatureStore<T> featuresNamed = storeFactory.createNamedFeatureList(features);
            return copyFeaturesCreateCustomName(featuresNamed);

        } catch (ProvisionFailedException e) {
            throw new OperationFailedException(e);
        }
    }

    private static <T extends FeatureInput> FeatureList<T> copyFeaturesCreateCustomName(
            NamedFeatureStore<T> features) throws OperationFailedException {
        try {
            return FeatureListFactory.mapFrom(
                    features, ni -> ni.getValue().duplicateChangeName(ni.getName()));

        } catch (BeanDuplicateException e) {
            throw new OperationFailedException(e);
        }
    }
}
