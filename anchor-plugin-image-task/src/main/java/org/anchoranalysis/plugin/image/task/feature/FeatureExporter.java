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

package org.anchoranalysis.plugin.image.task.feature;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;
import lombok.Getter;
import org.anchoranalysis.bean.NamedBean;
import org.anchoranalysis.bean.xml.exception.ProvisionFailedException;
import org.anchoranalysis.core.exception.CreateException;
import org.anchoranalysis.core.exception.OperationFailedException;
import org.anchoranalysis.core.time.ExecutionTimeRecorder;
import org.anchoranalysis.feature.bean.list.FeatureList;
import org.anchoranalysis.feature.bean.list.FeatureListProvider;
import org.anchoranalysis.feature.input.FeatureInput;
import org.anchoranalysis.feature.input.FeatureInputResults;
import org.anchoranalysis.feature.io.results.FeatureOutputMetadata;
import org.anchoranalysis.feature.io.results.FeatureOutputNames;
import org.anchoranalysis.feature.io.results.LabelHeaders;
import org.anchoranalysis.feature.io.results.calculation.FeatureCSVWriterCreator;
import org.anchoranalysis.feature.io.results.calculation.FeatureCalculationResults;
import org.anchoranalysis.feature.name.FeatureNameList;
import org.anchoranalysis.feature.store.NamedFeatureStore;
import org.anchoranalysis.feature.store.NamedFeatureStoreFactory;
import org.anchoranalysis.image.feature.calculator.FeatureTableCalculator;
import org.anchoranalysis.io.output.error.OutputWriteFailedException;
import org.anchoranalysis.io.output.outputter.InputOutputContext;
import org.anchoranalysis.plugin.image.task.bean.feature.ExportFeatures;

/**
 * Shared-state for an {@link ExportFeatures} task.
 *
 * @param <S> a source-of-rows that is duplicated for each new thread (to prevent any concurrency
 *     issues)
 * @author Owen Feehan
 */
public class FeatureExporter<S> {

    /** The output-name for writing thumbnails. */
    public static final String OUTPUT_THUMBNAILS = "thumbnails";

    /** Creates a {@link NamedFeatureStore}. */
    private static final NamedFeatureStoreFactory STORE_FACTORY =
            NamedFeatureStoreFactory.parametersOnly();

    /** The names of the features to be exported. */
    @Getter private final FeatureNameList featureNames;

    private final Supplier<S> featureSource;

    /** Saved store feature calculation results, and writes associated thumbnails. */
    @Getter private FeatureResultsAndThumbnails results;

    /** Context for outputting operations. */
    private FeatureExporterContext context;

    /**
     * Creates the shared state.
     *
     * @param outputMetadata headers and output-name for the feature CSV file that is written.
     * @param featureSource source of rows in the feature-table (called independently for each
     *     thread).
     * @param context context for exporting features.
     * @throws OutputWriteFailedException
     */
    public FeatureExporter(
            FeatureOutputMetadata outputMetadata,
            Supplier<S> featureSource,
            FeatureExporterContext context)
            throws OutputWriteFailedException {
        this.featureNames = outputMetadata.featureNamesNonAggregate();
        this.featureSource = featureSource;
        this.context = context;
        this.results = new FeatureResultsAndThumbnails(outputMetadata, context);
    }

    /**
     * Alternative static constructor that creates a shared-state from a list of named {@link
     * FeatureListProvider}s.
     *
     * @param <T> feature input-type in store
     * @param features a list of beans to create the features.
     * @param metadataHeaders headers to describe any metadata.
     * @param outputNames customizable output names used by {@link FeatureCalculationResults}.
     * @param context context.
     * @return a newly created {@link FeatureExporter}.
     * @throws CreateException if it cannot be successfully created.
     */
    public static <T extends FeatureInput> FeatureExporter<FeatureList<T>> create(
            List<NamedBean<FeatureListProvider<T>>> features,
            LabelHeaders metadataHeaders,
            FeatureOutputNames outputNames,
            FeatureExporterContext context)
            throws CreateException {
        try {
            return create(
                    STORE_FACTORY.createNamedFeatureList(features),
                    metadataHeaders,
                    outputNames,
                    context);
        } catch (ProvisionFailedException e) {
            throw new CreateException(e);
        }
    }

    /**
     * Alternative static constructor that creates a shared-state from a {@link NamedFeatureStore}.
     *
     * @param <T> feature input-type in store
     * @param featureStore a list of beans to create the features.
     * @param metadataHeaders headers to describe any metadata.
     * @param context context.
     * @return a newly created {@link FeatureExporter}.
     * @throws CreateException if it cannot be successfully created.
     */
    public static <T extends FeatureInput> FeatureExporter<FeatureList<T>> create(
            NamedFeatureStore<T> featureStore,
            LabelHeaders metadataHeaders,
            FeatureOutputNames outputNames,
            FeatureExporterContext context)
            throws CreateException {
        try {
            return new FeatureExporter<>(
                    new FeatureOutputMetadata(
                            metadataHeaders, featureStore.featureNames(), outputNames),
                    featureStore.duplicate()::features,
                    context);
        } catch (OutputWriteFailedException e) {
            throw new CreateException(e);
        }
    }

    /**
     * Alternative static constructor that creates a shared-state from a {@link NamedFeatureStore}.
     *
     * @param outputNames the names of the feature outputs.
     * @param features a table calculator for features.
     * @param identifierHeaders headers to describe the identifier metadata before the feature
     *     values.
     * @param context context.
     * @param <T> feature input-type in store
     * @return a newly created {@link FeatureExporter}.
     * @throws CreateException if it cannot be successfully created.
     */
    public static <T extends FeatureInput> FeatureExporter<FeatureTableCalculator<T>> create(
            FeatureOutputNames outputNames,
            FeatureTableCalculator<T> features,
            LabelHeaders identifierHeaders,
            FeatureExporterContext context)
            throws CreateException {
        try {
            return new FeatureExporter<>(
                    new FeatureOutputMetadata(
                            identifierHeaders, features.createFeatureNames(), outputNames),
                    features::duplicateForNewThread,
                    context);
        } catch (OutputWriteFailedException e) {
            throw new CreateException(e);
        }
    }

    /**
     * Creates a {@link FeatureCalculationContext} for calculating features to later use this
     * exporter.
     *
     * @param groupName the group to associate with the feature-results when outputted.
     * @param executionTimeRecorder records execution-times.
     * @param ioContext IO-context.
     * @return a newly created context.
     */
    public FeatureCalculationContext<S> createCalculationContext(
            Optional<String> groupName,
            ExecutionTimeRecorder executionTimeRecorder,
            InputOutputContext ioContext) {
        return new FeatureCalculationContext<>(
                results,
                featureSource.get(),
                featureNames,
                groupName,
                executionTimeRecorder,
                context.isSuppressErrors(),
                ioContext);
    }

    /**
     * Writes all the results that have been collected as a CSV file, and closes open I/O handles
     * and memory structures.
     *
     * @param featuresAggregate features that can be used for generating additional "aggregated"
     *     exports.
     * @param includeGroups iff true a group-column is included in the CSV file and the group
     *     exports occur, otherwise not.
     * @param csvWriterCreator creates a CSV writer for a particular IO-context.
     * @param context IO-context.
     * @throws OperationFailedException if any output cannot be written, or there is an error
     *     closing open I/O.
     */
    public void closeAndWriteOutputs(
            Optional<NamedFeatureStore<FeatureInputResults>> featuresAggregate,
            boolean includeGroups,
            Function<InputOutputContext, FeatureCSVWriterCreator> csvWriterCreator,
            InputOutputContext context)
            throws OperationFailedException {
        try {
            results.writeGroupedResults(
                    featuresAggregate, includeGroups, csvWriterCreator, context);
            results.closeAnyOpenIO();
            results = null;
        } catch (IOException | OutputWriteFailedException e) {
            throw new OperationFailedException(e);
        }
    }
}
