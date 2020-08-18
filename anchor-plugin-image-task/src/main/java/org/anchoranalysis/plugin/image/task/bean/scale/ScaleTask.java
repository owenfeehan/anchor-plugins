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

import ch.ethz.biol.cell.imageprocessing.chnl.provider.ChnlProviderScale;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.error.InitException;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.core.error.reporter.ErrorReporter;
import org.anchoranalysis.core.name.provider.NamedProvider;
import org.anchoranalysis.core.name.provider.NamedProviderGetException;
import org.anchoranalysis.core.progress.ProgressReporterNull;
import org.anchoranalysis.experiment.JobExecutionException;
import org.anchoranalysis.image.bean.nonbean.init.ImageInitParams;
import org.anchoranalysis.image.bean.scale.ScaleCalculator;
import org.anchoranalysis.image.binary.mask.Mask;
import org.anchoranalysis.image.channel.Channel;
import org.anchoranalysis.image.experiment.bean.task.RasterTask;
import org.anchoranalysis.image.interpolator.InterpolatorFactory;
import org.anchoranalysis.image.io.RasterIOException;
import org.anchoranalysis.image.io.input.ImageInitParamsFactory;
import org.anchoranalysis.image.io.input.NamedChnlsInput;
import org.anchoranalysis.image.io.input.series.NamedChannelsForSeries;
import org.anchoranalysis.image.io.stack.StackCollectionOutputter;
import org.anchoranalysis.image.stack.NamedStacksSet;
import org.anchoranalysis.image.stack.Stack;
import org.anchoranalysis.image.stack.wrap.WrapStackAsTimeSequenceStore;
import org.anchoranalysis.io.output.bound.BoundIOContext;
import org.anchoranalysis.io.output.bound.BoundOutputManagerRouteErrors;
import org.anchoranalysis.plugin.image.bean.mask.provider.resize.ScaleXY;

/**
 * Scales many rasters
 *
 * <p>Expects a second-level output "stack" to determine which stacks get ouputted or not
 *
 * @author Owen Feehan
 */
public class ScaleTask extends RasterTask {

    private static final String KEY_OUTPUT_STACK = "stack";

    // START BEAN PROPERTIES
    @BeanField @Getter @Setter private ScaleCalculator scaleCalculator;

    @BeanField @Getter @Setter private boolean forceBinary = false;
    // END BEAN PROPERTIES

    @Override
    public void startSeries(
            BoundOutputManagerRouteErrors outputManager, ErrorReporter errorReporter)
            throws JobExecutionException {
        // NOTHING TO DO
    }

    @Override
    public void doStack(
            NamedChnlsInput inputObject, int seriesIndex, int numSeries, BoundIOContext context)
            throws JobExecutionException {

        // Input
        NamedChannelsForSeries nccfs;
        try {
            nccfs = inputObject.createChannelsForSeries(0, ProgressReporterNull.get());
        } catch (RasterIOException e1) {
            throw new JobExecutionException(e1);
        }

        ImageInitParams soImage = ImageInitParamsFactory.create(context);

        try {
            // We store each channel as a stack in our collection, in case they need to be
            // referenced by the scale calculator
            nccfs.addAsSeparateChannels(
                    new WrapStackAsTimeSequenceStore(soImage.stacks()), 0);
            scaleCalculator.initRecursive(context.getLogger());
        } catch (InitException | OperationFailedException e) {
            throw new JobExecutionException(e);
        }

        populateAndOutputCollections(soImage, context);
    }

    private void populateAndOutputCollections(ImageInitParams soImage, BoundIOContext context)
            throws JobExecutionException {
        // Our output collections
        NamedStacksSet stackCollection = new NamedStacksSet();
        NamedStacksSet stackCollectionMIP = new NamedStacksSet();

        populateOutputCollectionsFromSharedObjects(
                soImage, stackCollection, stackCollectionMIP, context);

        outputStackCollection(stackCollection, KEY_OUTPUT_STACK, "chnlScaledCollection", context);
        outputStackCollection(
                stackCollectionMIP, KEY_OUTPUT_STACK, "chnlScaledCollectionMIP", context);
    }

    @Override
    public boolean hasVeryQuickPerInputExecution() {
        return false;
    }

    private static void outputStackCollection(
            NamedProvider<Stack> stackCollection,
            String outputSecondLevelKey,
            String outputName,
            BoundIOContext context) {
        BoundOutputManagerRouteErrors outputManager = context.getOutputManager();

        StackCollectionOutputter.output(
                StackCollectionOutputter.subset(
                        stackCollection,
                        outputManager.outputAllowedSecondLevel(outputSecondLevelKey)),
                outputManager.getDelegate(),
                outputName,
                "",
                context.getErrorReporter(),
                false);
    }

    private void populateOutputCollectionsFromSharedObjects(
            ImageInitParams params,
            NamedStacksSet stackCollection,
            NamedStacksSet stackCollectionMIP,
            BoundIOContext context)
            throws JobExecutionException {

        Set<String> chnlNames = params.stacks().keys();
        for (String chnlName : chnlNames) {

            // If this output is not allowed we simply skip
            if (!context.getOutputManager()
                    .outputAllowedSecondLevel(KEY_OUTPUT_STACK)
                    .isOutputAllowed(chnlName)) {
                continue;
            }

            try {
                Channel chnlIn = params.stacks().getException(chnlName).getChannel(0);

                Channel chnlOut;
                if (forceBinary) {
                    Mask mask = new Mask(chnlIn);
                    chnlOut = ScaleXY.scale(mask, scaleCalculator).channel();
                } else {
                    chnlOut =
                            ChnlProviderScale.scale(
                                    chnlIn,
                                    scaleCalculator,
                                    InterpolatorFactory.getInstance().rasterResizing(),
                                    context.getLogger().messageLogger());
                }

                stackCollection.addImageStack(chnlName, new Stack(chnlOut));
                stackCollectionMIP.addImageStack(
                        chnlName, new Stack(chnlOut.maxIntensityProjection()));

            } catch (CreateException e) {
                throw new JobExecutionException(e);
            } catch (NamedProviderGetException e) {
                throw new JobExecutionException(e.summarize());
            }
        }
    }

    @Override
    public void endSeries(BoundOutputManagerRouteErrors outputManager)
            throws JobExecutionException {
        // NOTHING TO DO
    }
}
