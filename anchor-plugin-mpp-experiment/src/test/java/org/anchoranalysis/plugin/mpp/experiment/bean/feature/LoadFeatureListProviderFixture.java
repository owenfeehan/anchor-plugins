/*-
 * #%L
 * anchor-plugin-mpp-experiment
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

package org.anchoranalysis.plugin.mpp.experiment.bean.feature;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.anchoranalysis.bean.NamedBean;
import org.anchoranalysis.core.exception.CreateException;
import org.anchoranalysis.core.exception.friendly.AnchorFriendlyRuntimeException;
import org.anchoranalysis.feature.bean.Feature;
import org.anchoranalysis.feature.bean.list.Define;
import org.anchoranalysis.feature.bean.list.FeatureListProvider;
import org.anchoranalysis.feature.input.FeatureInput;
import org.anchoranalysis.test.TestLoader;
import org.anchoranalysis.test.feature.plugins.FeaturesFromXmlFixture;
import lombok.RequiredArgsConstructor;

/**
 * Loads a feature-list to be used in tests.
 *
 * <p>By default, this comes from a particular XML file in the test-resources, but methods allow
 * other possibilities
 *
 * @param <T> feature-input-type returned by the list
 * @author Owen Feehan
 */
@RequiredArgsConstructor
class LoadFeatureListProviderFixture<T extends FeatureInput> {

    private static final String SINGLE_FEATURE_NAME = "someFeature";

    // START REQUIRED ARGUMENTS
    /** A loader for finding XML files. */
    private final TestLoader loader;
    
    /** The features in this XML file are used if no other option is set. */
    private final String defaultPathToXML;
    // END REQUIRED ARGUMENTS

    /** The to use, if set. If empty, the default is used instead. */
    private Optional<List<NamedBean<FeatureListProvider<T>>>> features = Optional.empty();

    /**
     * Additionally include a shell feature in the "single" features
     *
     * @param alternativePathToXml
     */
    public void useAlternativeXMLList(String alternativePathToXml) {
        features = Optional.of(loadFeatures(loader, alternativePathToXml));
    }

    /**
     * Uses this feature as the only item in the list.
     *
     * <p>Any XML-based features are ignored.
     *
     * <p>It does not initialize the feature.
     */
    public void useSingleFeature(Feature<T> feature) {

        NamedBean<FeatureListProvider<T>> nb =
                new NamedBean<>(SINGLE_FEATURE_NAME, new Define<>(feature));

        this.features = Optional.of(Arrays.asList(nb));
    }

    /** Creates the features as a list of providers in named-beans */
    public List<NamedBean<FeatureListProvider<T>>> asListNamedBeansProvider() {
        if (features.isPresent()) {
            return features.get();
        } else {
            return loadFeatures(loader, defaultPathToXML);
        }
    }

    /** Creates a feature-list associated with object-mask. */
    private static <S extends FeatureInput> List<NamedBean<FeatureListProvider<S>>> loadFeatures(
            TestLoader loader, String pathFeatureList) {
        try {
            return FeaturesFromXmlFixture.createNamedFeatureProviders(pathFeatureList, loader);
        } catch (CreateException e) {
            // Prefer run-time exceptions to avoid chaining in the tests
            throw new AnchorFriendlyRuntimeException(e);
        }
    }
}
