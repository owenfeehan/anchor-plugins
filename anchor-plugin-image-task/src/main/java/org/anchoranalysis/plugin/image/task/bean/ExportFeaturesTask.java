/*-
 * #%L
 * anchor-plugin-image-task
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

package org.anchoranalysis.plugin.image.task.bean;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.NamedBean;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.annotation.NonEmpty;
import org.anchoranalysis.bean.annotation.OptionalBean;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.core.functional.OptionalUtilities;
import org.anchoranalysis.experiment.ExperimentExecutionException;
import org.anchoranalysis.experiment.JobExecutionException;
import org.anchoranalysis.experiment.task.InputBound;
import org.anchoranalysis.experiment.task.InputTypesExpected;
import org.anchoranalysis.experiment.task.ParametersExperiment;
import org.anchoranalysis.experiment.task.Task;
import org.anchoranalysis.feature.bean.list.FeatureListProvider;
import org.anchoranalysis.feature.input.FeatureInput;
import org.anchoranalysis.feature.list.NamedFeatureStore;
import org.anchoranalysis.feature.list.NamedFeatureStoreFactory;
import org.anchoranalysis.feature.resultsvectorcollection.FeatureInputResults;
import org.anchoranalysis.io.bean.filepath.generator.FilePathGenerator;
import org.anchoranalysis.io.error.AnchorIOException;
import org.anchoranalysis.io.filepath.FilePathToUnixStyleConverter;
import org.anchoranalysis.io.input.InputFromManager;
import org.anchoranalysis.io.output.bound.BoundIOContext;
import org.anchoranalysis.io.output.bound.BoundOutputManagerRouteErrors;
import org.anchoranalysis.plugin.image.task.bean.feature.source.FeatureSource;
import org.anchoranalysis.plugin.image.task.feature.ExportFeatureFromInputContext;
import org.anchoranalysis.plugin.image.task.feature.SharedStateExportFeatures;

/**
 * Calculates features and exports them as a CSV
 *
 * <p><Aggregated-features (based upon a certain grouping) can also be calculated.
 *
 * <p>Types of exports are:
 *
 * <table>
 * <tr><td>features</td><td>a single csv file where each row is an object</td></tr>
 * <tr><td>featuresAggregated</td><td>a single csv file where each row is a group (with aggregated features of the objects within)</td></tr>
 * <tr><td>featuresGroup</td><td>a csv file per group, where each row is an object</td></tr>
 * </table>
 *
 * @author Owen Feehan
 * @param <T> See {@link Task}
 * @param <S> a source-of-features that is duplicated for each new thread (to prevent any
 *     concurrency issues)
 * @param <U> feature-input type for {@code features} bean-field
 */
public class ExportFeaturesTask<T extends InputFromManager, S, U extends FeatureInput>
        extends Task<T, SharedStateExportFeatures<S>> {

    private static final NamedFeatureStoreFactory STORE_FACTORY_AGGREGATE =
            NamedFeatureStoreFactory.bothNameAndParams();

    // START BEAN PROPERTIES
    /** Source of feature-values to be exported */
    @BeanField @Getter @Setter private FeatureSource<T, S, U> source;

    /**
     * If non-null this file-path is used to determine the group of the file If null, no group is
     * included.
     */
    @BeanField @OptionalBean @Getter @Setter private FilePathGenerator group;

    /** Translates an input file name to a unique ID */
    @BeanField @OptionalBean @Getter @Setter private FilePathGenerator id;

    /** The features to be exported (after possibly some manipulation or augmentation) */
    @BeanField @NonEmpty @Getter @Setter
    private List<NamedBean<FeatureListProvider<U>>> features = new ArrayList<>();

    /** Features applied to each group to aggregate values (takes FeatureResultsVectorCollection) */
    @BeanField @OptionalBean @Getter @Setter
    private List<NamedBean<FeatureListProvider<FeatureInputResults>>> featuresAggregate;
    
    /** 
     * Iff true, a thumbnail-image is generated as a visualization of each row that is exported
     *   (but only if supported by source). 
     */
    @BeanField @Getter @Setter private boolean thumbnails = false;
    // END BEAN PROPERTIES

    @Override
    public SharedStateExportFeatures<S> beforeAnyJobIsExecuted(
            BoundOutputManagerRouteErrors outputManager, ParametersExperiment params)
            throws ExperimentExecutionException {
        try {
            return source.createSharedState(
                    source.headers().createMetadataHeaders(isGroupGeneratorDefined()),
                    features,
                    params.getContext());
        } catch (CreateException e) {
            throw new ExperimentExecutionException(e);
        }
    }

    @Override
    public void doJobOnInputObject(InputBound<T, SharedStateExportFeatures<S>> input)
            throws JobExecutionException {
        try {
            Optional<String> groupName =
                    extractGroupNameFromGenerator(
                            input.getInputObject().pathForBindingRequired(),
                            input.context().isDebugEnabled());
            
            ExportFeatureFromInputContext context = new ExportFeatureFromInputContext(
                input.getSharedState().getFeatureNames(),
                groupName,
                thumbnails,
                input.context()
            );
            
            source.processInput(
                    input.getInputObject(),
                    input.getSharedState().addResultsFor(),
                    input.getSharedState().duplicateForNewThread(),
                    context);
            
        } catch (OperationFailedException | AnchorIOException e) {
            throw new JobExecutionException(e);
        }
    }

    @Override
    public void afterAllJobsAreExecuted(
            SharedStateExportFeatures<S> sharedState, BoundIOContext context)
            throws ExperimentExecutionException {

        try {
            sharedState.closeAnyOpenIO();
            sharedState.writeGroupedResults(
                    featuresAggregateAsStore(),
                    source.includeGroupInExperiment(isGroupGeneratorDefined()),
                    context);
        } catch (AnchorIOException | CreateException | IOException e) {
            throw new ExperimentExecutionException(e);
        }
    }

    @Override
    public boolean hasVeryQuickPerInputExecution() {
        return false;
    }

    @Override
    public InputTypesExpected inputTypesExpected() {
        return source.inputTypesExpected();
    }

    private boolean isGroupGeneratorDefined() {
        return group != null;
    }

    private static Optional<String> filePathAsIdentifier(
            Optional<FilePathGenerator> generator, Path path, boolean debugMode)
            throws AnchorIOException {
        return OptionalUtilities.map(
                generator,
                gen ->
                        FilePathToUnixStyleConverter.toStringUnixStyle(
                                gen.outFilePath(path, debugMode)));
    }

    /** Determines the group name corresponding to an inputPath and the group-generator */
    private Optional<String> extractGroupNameFromGenerator(Path inputPath, boolean debugMode)
            throws AnchorIOException {
        return filePathAsIdentifier(Optional.ofNullable(group), inputPath, debugMode);
    }

    private Optional<NamedFeatureStore<FeatureInputResults>> featuresAggregateAsStore()
            throws CreateException {
        return OptionalUtilities.map(
                Optional.ofNullable(featuresAggregate),
                STORE_FACTORY_AGGREGATE::createNamedFeatureList);
    }
}
