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
import lombok.Getter;
import lombok.experimental.Accessors;
import org.anchoranalysis.bean.NamedBean;
import org.anchoranalysis.bean.error.BeanMisconfiguredException;
import org.anchoranalysis.bean.xml.RegisterBeanFactories;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.feature.input.FeatureInput;
import org.anchoranalysis.feature.nrg.NRGStack;
import org.anchoranalysis.image.bean.provider.ObjectCollectionProvider;
import org.anchoranalysis.image.feature.object.input.FeatureInputSingleObject;
import org.anchoranalysis.image.feature.session.FeatureTableCalculator;
import org.anchoranalysis.io.bean.filepath.generator.FilePathGeneratorConstant;
import org.anchoranalysis.mpp.io.input.MultiInput;
import org.anchoranalysis.plugin.image.bean.object.provider.Reference;
import org.anchoranalysis.plugin.image.feature.bean.object.combine.CombineObjectsForFeatures;
import org.anchoranalysis.plugin.image.feature.bean.object.combine.EachObjectIndependently;
import org.anchoranalysis.plugin.image.feature.bean.object.combine.PairNeighbors;
import org.anchoranalysis.plugin.image.task.bean.ExportFeaturesTask;
import org.anchoranalysis.plugin.mpp.experiment.bean.feature.source.FromObjects;
import org.anchoranalysis.test.TestLoader;
import org.anchoranalysis.test.image.NRGStackFixture;

@Accessors(fluent = true)
class TaskFixture {

    @Getter private NRGStack nrgStack;

    @Getter private final ExportObjectsFeatureLoader featureLoader;

    private CombineObjectsForFeatures<?> flexiFeatureTable = new EachObjectIndependently();

    /**
     * Constructor
     *
     * <p>By default, use a big-sized NRG-stack that functions with our feature-lists
     *
     * <p>By default, load the features from PATH_FEATURES
     *
     * <p>By default, use Simple feature-mode. It can be changed to Merged-Pairs.
     *
     * @param loader
     * @throws CreateException
     */
    public TaskFixture(TestLoader loader) throws CreateException {
        this.nrgStack = createNRGStack(true);
        this.featureLoader = new ExportObjectsFeatureLoader(loader);
    }

    /** Change to using a small nrg-stack that causes some features to throw errors */
    public void useSmallNRGInstead() {
        this.nrgStack = createNRGStack(false);
    }

    /**
     * Change to use Merged-Pairs mode rather than Simple mode
     *
     * @param includeFeaturesInPair iff TRUE "pair" features are populated in merged-pair mode
     */
    public void changeToMergedPairs(boolean includeFeaturesInPair, boolean includeImageFeatures) {
        flexiFeatureTable = createMergedPairs(includeFeaturesInPair, includeImageFeatures);
    }

    public <T extends FeatureInput>
            ExportFeaturesTask<MultiInput, FeatureTableCalculator<T>, FeatureInputSingleObject>
                    createTask() throws CreateException {

        ExportFeaturesTask<MultiInput, FeatureTableCalculator<T>, FeatureInputSingleObject> task =
                new ExportFeaturesTask<>();
        task.setSource(createSource());
        task.setFeatures(featureLoader.single());
        task.setFeaturesAggregate(featureLoader.aggregated());
        task.setGroup(new FilePathGeneratorConstant("arbitraryGroup"));

        try {
            task.checkMisconfigured(RegisterBeanFactories.getDefaultInstances());
        } catch (BeanMisconfiguredException e) {
            throw new CreateException(e);
        }

        return task;
    }

    @SuppressWarnings("unchecked")
    private <T extends FeatureInput> FromObjects<T> createSource() throws CreateException {
        FromObjects<T> source = new FromObjects<>();
        source.setDefine(DefineFixture.create(nrgStack, Optional.of(featureLoader.shared())));
        source.setCombine((CombineObjectsForFeatures<T>) flexiFeatureTable);
        source.setObjects(createObjectProviders(MultiInputFixture.OBJECTS_NAME));
        return source;
    }

    private static List<NamedBean<ObjectCollectionProvider>> createObjectProviders(
            String objectsName) {
        return Arrays.asList(new NamedBean<>(objectsName, new Reference(objectsName)));
    }

    private PairNeighbors createMergedPairs(
            boolean includeFeaturesInPair, boolean includeImageFeatures) {
        PairNeighbors mergedPairs = new PairNeighbors();
        if (includeFeaturesInPair) {
            mergedPairs.setFeaturesPair(featureLoader.pair());
        }
        if (includeImageFeatures) {
            mergedPairs.setFeaturesImage(featureLoader.image());
        }
        return mergedPairs;
    }

    private NRGStack createNRGStack(boolean bigSizeNrg) {
        return NRGStackFixture.create(bigSizeNrg, false).getNrgStack();
    }
}
