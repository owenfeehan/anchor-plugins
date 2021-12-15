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

package org.anchoranalysis.plugin.image.task.bean.grouped;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.annotation.DefaultInstance;
import org.anchoranalysis.bean.annotation.OptionalBean;
import org.anchoranalysis.core.exception.OperationFailedException;
import org.anchoranalysis.core.log.Logger;
import org.anchoranalysis.experiment.ExperimentExecutionException;
import org.anchoranalysis.experiment.JobExecutionException;
import org.anchoranalysis.experiment.bean.task.Task;
import org.anchoranalysis.experiment.task.InputBound;
import org.anchoranalysis.experiment.task.InputTypesExpected;
import org.anchoranalysis.experiment.task.ParametersExperiment;
import org.anchoranalysis.image.bean.interpolator.Interpolator;
import org.anchoranalysis.image.core.stack.named.NamedStacks;
import org.anchoranalysis.image.io.stack.input.ProvidesStackInput;
import org.anchoranalysis.inference.concurrency.ConcurrencyPlan;
import org.anchoranalysis.io.input.bean.path.DerivePath;
import org.anchoranalysis.io.input.path.DerivePathException;
import org.anchoranalysis.io.manifest.ManifestDirectoryDescription;
import org.anchoranalysis.io.manifest.sequencetype.StringsWithoutOrder;
import org.anchoranalysis.io.output.outputter.InputOutputContext;
import org.anchoranalysis.io.output.outputter.Outputter;
import org.anchoranalysis.plugin.image.task.bean.grouped.selectchannels.All;
import org.anchoranalysis.plugin.image.task.bean.grouped.selectchannels.FromStacks;
import org.anchoranalysis.plugin.image.task.grouped.ConsistentChannelChecker;
import org.anchoranalysis.plugin.image.task.grouped.GroupMapByName;
import org.anchoranalysis.plugin.image.task.grouped.GroupedSharedState;

/**
 * Base class for stacks (usually each channel from an image) that are somehow grouped-together.
 *
 * @author Owen Feehan
 * @param <S> individual-type
 * @param <T> aggregate-type
 */
public abstract class GroupedStackBase<S, T>
        extends Task<ProvidesStackInput, GroupedSharedState<S, T>> {

    // START BEAN PROPERTIES
    /** The interpolator to use for scaling images. */
    @BeanField @Getter @Setter @DefaultInstance private Interpolator interpolator;
    // END BEAN PROPERTIES

    private static final ManifestDirectoryDescription MANIFEST_DESCRIPTION_GROUP_FOLDER =
            new ManifestDirectoryDescription(
                    "groupedDirectory", "groupedStack", new StringsWithoutOrder());

    // START BEAN PROPERTIES
    /**
     * If defined, translates a file-path into a group. If not-defined, all images are treated as
     * part of the same group
     */
    @BeanField @OptionalBean @Getter @Setter private DerivePath group;

    /** Selects which channels are included, optionally renaming. */
    @BeanField @Getter @Setter private FromStacks selectChannels = new All();
    // END BEAN PROPERTIES

    @Override
    public InputTypesExpected inputTypesExpected() {
        return new InputTypesExpected(ProvidesStackInput.class);
    }

    @Override
    public boolean hasVeryQuickPerInputExecution() {
        return false;
    }

    @Override
    public GroupedSharedState<S, T> beforeAnyJobIsExecuted(
            Outputter outputter,
            ConcurrencyPlan concurrencyPlan,
            List<ProvidesStackInput> inputs,
            ParametersExperiment parameters)
            throws ExperimentExecutionException {
        return new GroupedSharedState<>(this::createGroupMap);
    }

    @Override
    public void doJobOnInput(InputBound<ProvidesStackInput, GroupedSharedState<S, T>> input)
            throws JobExecutionException {

        ProvidesStackInput inputStack = input.getInput();
        InputOutputContext context = input.getContextJob();

        // Extract a group name
        Optional<String> groupName =
                extractGroupName(inputStack.pathForBinding(), context.isDebugEnabled());

        processStacks(
                GroupedStackBase.extractInputStacks(inputStack, context.getLogger()),
                groupName,
                input.getSharedState(),
                context);
    }

    @Override
    public void afterAllJobsAreExecuted(
            GroupedSharedState<S, T> sharedState, InputOutputContext context)
            throws ExperimentExecutionException {

        try {
            Optional<String> subdirectoryName = subdirectoryForGroupOutputs();
            if (context.getOutputter().outputsEnabled().isOutputEnabled(outputNameForGroups())) {
                sharedState
                        .getGroupMap()
                        .outputGroupedData(
                                sharedState.getChannelChecker(),
                                context.maybeSubdirectory(
                                        subdirectoryName,
                                        MANIFEST_DESCRIPTION_GROUP_FOLDER,
                                        false));
            }

        } catch (IOException e) {
            throw new ExperimentExecutionException(e);
        }
    }

    /**
     * The first-level output-name used for determining if groups are written.
     *
     * <p>Second-level matches against this, will determine which specific groups may or may not be
     * written.
     *
     * @return
     */
    protected abstract String outputNameForGroups();

    /** An optional subdirectory where the group outputs are placed. */
    protected abstract Optional<String> subdirectoryForGroupOutputs();

    /**
     * Creates a map for the storing an aggregate-data-object for each group.
     *
     * @param channelChecker checks that the channels of all relevant stacks have the same size and
     *     data-type.
     * @return a newly created map
     */
    protected abstract GroupMapByName<S, T> createGroupMap(ConsistentChannelChecker channelChecker);

    /**
     * Processes one set of named-stacks.
     *
     * @param stacks the named-stacks (usually each channel from an image).
     * @param groupName the name of the group
     * @param sharedState shared-state
     * @param context context for reading/writing
     * @throws JobExecutionException if anything goes wrong
     */
    protected abstract void processStacks(
            NamedStacks stacks,
            Optional<String> groupName,
            GroupedSharedState<S, T> sharedState,
            InputOutputContext context)
            throws JobExecutionException;

    private Optional<String> extractGroupName(Optional<Path> path, boolean debugEnabled)
            throws JobExecutionException {

        // 	Return an arbitrary group-name if there's no binding-path, or a group-generator is not
        // defined
        if (group == null || !path.isPresent()) {
            return Optional.empty();
        }

        try {
            return Optional.of(group.deriveFrom(path.get(), debugEnabled).toString());
        } catch (DerivePathException e) {
            throw new JobExecutionException(
                    String.format("Cannot establish a group-identifier for: %s", path), e);
        }
    }

    private static NamedStacks extractInputStacks(ProvidesStackInput input, Logger logger)
            throws JobExecutionException {
        try {
            NamedStacks stacks = new NamedStacks();
            input.addToStoreInferNames(stacks, logger);
            return stacks;
        } catch (OperationFailedException e1) {
            throw new JobExecutionException("An error occurred creating inputs to the task", e1);
        }
    }
}
