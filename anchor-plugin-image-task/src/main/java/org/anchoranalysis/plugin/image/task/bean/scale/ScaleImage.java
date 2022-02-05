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

package org.anchoranalysis.plugin.image.task.bean.scale;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.annotation.DefaultInstance;
import org.anchoranalysis.bean.xml.exception.ProvisionFailedException;
import org.anchoranalysis.core.exception.OperationFailedException;
import org.anchoranalysis.core.identifier.provider.NamedProviderGetException;
import org.anchoranalysis.core.log.MessageLogger;
import org.anchoranalysis.experiment.ExperimentExecutionException;
import org.anchoranalysis.experiment.JobExecutionException;
import org.anchoranalysis.experiment.bean.task.Task;
import org.anchoranalysis.experiment.task.InputBound;
import org.anchoranalysis.experiment.task.InputTypesExpected;
import org.anchoranalysis.experiment.task.NoSharedState;
import org.anchoranalysis.experiment.task.ParametersExperiment;
import org.anchoranalysis.image.bean.interpolator.Interpolator;
import org.anchoranalysis.image.bean.nonbean.init.ImageInitialization;
import org.anchoranalysis.image.bean.spatial.ScaleCalculator;
import org.anchoranalysis.image.core.channel.Channel;
import org.anchoranalysis.image.core.dimensions.size.suggestion.ImageSizeSuggestion;
import org.anchoranalysis.image.core.mask.Mask;
import org.anchoranalysis.image.core.stack.Stack;
import org.anchoranalysis.image.core.stack.named.NamedStacks;
import org.anchoranalysis.image.io.stack.input.StackSequenceInput;
import org.anchoranalysis.inference.concurrency.ConcurrencyPlan;
import org.anchoranalysis.io.output.enabled.OutputEnabledMutable;
import org.anchoranalysis.io.output.outputter.InputOutputContext;
import org.anchoranalysis.io.output.outputter.Outputter;
import org.anchoranalysis.plugin.image.bean.channel.provider.intensity.ScaleXY;
import org.anchoranalysis.plugin.image.task.stack.InitializationFactory;

/**
 * Creates a scaled copy of images.
 *
 * <p>An RGB image is treated as a single-stack, otherwise each channel is scaled and outputted
 * separately.
 *
 * <p>Second-level output keys for <i>scaled</i> and/or <i>scaledFlattened</i> additionally
 * determine which stacks get outputted or not.
 *
 * <p>Any z-dimension present is unaffected by the scaling in {@code scaled}.
 *
 * <p>The following outputs are produced:
 *
 * <table>
 * <caption></caption>
 * <thead>
 * <tr><th>Output Name</th><th>Default?</th><th>Description</th></tr>
 * </thead>
 * <tbody>
 * <tr><td>{@value ScaleImage#OUTPUT_SCALED}</td><td>yes</td><td>A scaled copy of the input image.</td></tr>
 * <tr><td>{@value ScaleImage#OUTPUT_SCALED_FLATTENED}</td><td>no</td><td>A scaled copy of the maximum-intensity-projection of the input image.</td></tr>
 * <tr><td rowspan="3"><i>inherited from {@link Task}</i></td></tr>
 * </tbody>
 * </table>
 *
 * @author Owen Feehan
 */
public class ScaleImage extends Task<StackSequenceInput, NoSharedState> {

    /** Output-name for a scaled copy of the input image. */
    private static final String OUTPUT_SCALED = "scaled";

    /** Output-name for a scaled copy the maximum-intensity-projection of the input image. */
    private static final String OUTPUT_SCALED_FLATTENED = "scaledFlattened";

    // START BEAN PROPERTIES
    /** Calculates what scale-factor to apply on the image. */
    @BeanField @Getter @Setter private ScaleCalculator scaleCalculator;

    /**
     * Iff true the image to be scaled is treated as a binary-mask, and interpolation during scaling
     * ensures only two binary-values are ouputted.
     */
    @BeanField @Getter @Setter private boolean binary = false;

    /** The interpolator to use for scaling images. */
    @BeanField @Getter @Setter @DefaultInstance private Interpolator interpolator;
    // END BEAN PROPERTIES

    @Override
    public InputTypesExpected inputTypesExpected() {
        return new InputTypesExpected(StackSequenceInput.class);
    }

    @Override
    public NoSharedState beforeAnyJobIsExecuted(
            Outputter outputter,
            ConcurrencyPlan concurrencyPlan,
            List<StackSequenceInput> inputs,
            ParametersExperiment parameters)
            throws ExperimentExecutionException {
        return NoSharedState.INSTANCE;
    }

    @Override
    public void doJobOnInput(InputBound<StackSequenceInput, NoSharedState> input)
            throws JobExecutionException {
        try {
            NamedStacks stacks = input.getInput().asSet(input.getLogger());

            ImageInitialization initialization =
                    InitializationFactory.createWithoutStacks(input.createInitializationContext());
            // We store each channel as a stack in our collection, in case they need to be
            // referenced by the scale calculator
            initialization.addStacksFrom(stacks);

            populateAndOutput(initialization, input.getContextJob());
        } catch (OperationFailedException e) {
            throw new JobExecutionException(e);
        }
    }

    @Override
    public void afterAllJobsAreExecuted(NoSharedState sharedState, InputOutputContext context)
            throws ExperimentExecutionException {
        // NOTHING TO DO
    }

    @Override
    public boolean hasVeryQuickPerInputExecution() {
        return false;
    }

    @Override
    public OutputEnabledMutable defaultOutputs() {
        return super.defaultOutputs().addEnabledOutputFirst(OUTPUT_SCALED);
    }

    private void populateAndOutput(ImageInitialization initialization, InputOutputContext context)
            throws JobExecutionException {

        // Output collections
        DualNamedStacks stacks = new DualNamedStacks();

        DualEnabled dualEnabled =
                new DualEnabled(
                        OutputterHelper.isFirstLevelOutputEnabled(OUTPUT_SCALED, context),
                        OutputterHelper.isFirstLevelOutputEnabled(
                                OUTPUT_SCALED_FLATTENED, context));
        if (dualEnabled.isEitherEnabled()) {
            populateStacksFromSharedObjects(initialization, stacks, dualEnabled, context);
            OutputterHelper.outputStacks(
                    stacks,
                    dualEnabled,
                    OUTPUT_SCALED,
                    OUTPUT_SCALED_FLATTENED,
                    context.getOutputter().getChecked());
        }
    }

    private void populateStacksFromSharedObjects(
            ImageInitialization initialization,
            DualNamedStacks stacksToAddTo,
            DualEnabled dualEnabled,
            InputOutputContext context)
            throws JobExecutionException {
        Set<String> inputKeys = initialization.stacks().keys();
        for (String key : inputKeys) {

            DualEnabled enabledForKey =
                    dualEnabled.and(
                            () ->
                                    OutputterHelper.isSecondLevelOutputEnabled(
                                            OUTPUT_SCALED, key, context),
                            () ->
                                    OutputterHelper.isSecondLevelOutputEnabled(
                                            OUTPUT_SCALED_FLATTENED, key, context));

            if (enabledForKey.isEitherEnabled()) {
                try {
                    Stack stackIn = initialization.stacks().getException(key);

                    Stack stackOut =
                            scaleStack(
                                    stackIn,
                                    initialization.suggestedSize(),
                                    context.getLogger().messageLogger());

                    stacksToAddTo.addStack(key, stackOut, enabledForKey);

                } catch (OperationFailedException e) {
                    throw new JobExecutionException(e);
                } catch (NamedProviderGetException e) {
                    throw new JobExecutionException(e.summarize());
                }
            }
        }
    }

    private Stack scaleStack(
            Stack stack, Optional<ImageSizeSuggestion> suggestedSize, MessageLogger logger)
            throws OperationFailedException {
        return stack.mapChannel(channel -> scaleChannel(channel, suggestedSize, logger));
    }

    private Channel scaleChannel(
            Channel channel, Optional<ImageSizeSuggestion> suggestedSize, MessageLogger logger)
            throws OperationFailedException {
        try {
            if (binary) {
                return scaleChannelAsMask(channel, suggestedSize);
            } else {
                return ScaleXY.scale(
                        channel,
                        scaleCalculator,
                        interpolator.voxelsResizer(),
                        suggestedSize,
                        logger);
            }
        } catch (ProvisionFailedException e) {
            throw new OperationFailedException(e);
        }
    }

    private Channel scaleChannelAsMask(Channel channel, Optional<ImageSizeSuggestion> suggestedSize)
            throws ProvisionFailedException {
        Mask mask = new Mask(channel);
        Mask maskScaled =
                org.anchoranalysis.plugin.image.bean.mask.provider.ScaleXY.scale(
                        mask, scaleCalculator, suggestedSize);
        return maskScaled.channel();
    }
}
