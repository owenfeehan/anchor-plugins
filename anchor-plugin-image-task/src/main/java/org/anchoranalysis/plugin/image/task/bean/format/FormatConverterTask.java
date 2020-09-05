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

package org.anchoranalysis.plugin.image.task.bean.format;

import java.util.Set;
import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.annotation.OptionalBean;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.error.reporter.ErrorReporter;
import org.anchoranalysis.core.log.Logger;
import org.anchoranalysis.core.progress.ProgressReporterConsole;
import org.anchoranalysis.core.progress.ProgressReporterNull;
import org.anchoranalysis.experiment.JobExecutionException;
import org.anchoranalysis.image.bean.channel.converter.ConvertChannelTo;
import org.anchoranalysis.image.channel.convert.ConversionPolicy;
import org.anchoranalysis.image.experiment.bean.task.RasterTask;
import org.anchoranalysis.image.io.RasterIOException;
import org.anchoranalysis.image.io.bean.channel.ChannelFilter;
import org.anchoranalysis.image.io.channel.ChannelGetter;
import org.anchoranalysis.image.io.generator.raster.StackGenerator;
import org.anchoranalysis.image.io.input.NamedChannelsInput;
import org.anchoranalysis.image.io.input.series.NamedChannelsForSeries;
import org.anchoranalysis.image.stack.Stack;
import org.anchoranalysis.io.error.AnchorIOException;
import org.anchoranalysis.io.generator.sequence.GeneratorSequenceNonIncrementalRerouterErrors;
import org.anchoranalysis.io.generator.sequence.GeneratorSequenceNonIncrementalWriter;
import org.anchoranalysis.io.manifest.sequencetype.SetSequenceType;
import org.anchoranalysis.io.namestyle.StringSuffixOutputNameStyle;
import org.anchoranalysis.io.output.bound.BoundIOContext;
import org.anchoranalysis.io.output.bound.BoundOutputManagerRouteErrors;
import org.anchoranalysis.plugin.image.task.bean.format.convertsyle.ChannelConvertStyle;
import org.anchoranalysis.plugin.image.task.channel.ChannelGetterForTimepoint;

/**
 * Converts the input-image to the default output format, optionally changing the bit depth
 *
 * <p>If it looks like an RGB image, channels are written together. Otherwise they are written
 * independently
 *
 * @author Owen Feehan
 */
public class FormatConverterTask extends RasterTask {

    // START BEAN PROPERTIES

    /** To convert as RGB or independently or in another way */
    @BeanField @Getter @Setter private ChannelConvertStyle channelConversionStyle = null;

    @BeanField @Getter @Setter private boolean suppressSeries = false;

    @BeanField @OptionalBean @Getter @Setter private ChannelFilter channelFilter = null;

    @BeanField @OptionalBean @Getter @Setter private ConvertChannelTo channelConverter = null;
    // END BEAN PROPERTIES

    private GeneratorSequenceNonIncrementalRerouterErrors<Stack> generatorSeq;

    public FormatConverterTask() {
        super();
    }

    @Override
    public void startSeries(
            BoundOutputManagerRouteErrors outputManager, ErrorReporter errorReporter)
            throws JobExecutionException {

        StackGenerator generator = new StackGenerator(false, "out");

        generatorSeq =
                new GeneratorSequenceNonIncrementalRerouterErrors<>(
                        new GeneratorSequenceNonIncrementalWriter<>(
                                outputManager.getDelegate(),
                                "",
                                // NOTE WE ARE NOT ASSIGNING A NAME TO THE OUTPUT
                                new StringSuffixOutputNameStyle("", "%s"),
                                generator,
                                true),
                        errorReporter);
        generatorSeq.setSuppressSubfolder(true);

        // TODO it would be nicer to reflect the real sequence type, than just using a set of
        // indexes
        generatorSeq.start(new SetSequenceType(), -1);
    }

    @Override
    public boolean hasVeryQuickPerInputExecution() {
        return false;
    }

    public NamedChannelsForSeries createChannelCollection(
            NamedChannelsInput inputObject, int seriesIndex) throws RasterIOException {
        return inputObject.createChannelsForSeries(seriesIndex, new ProgressReporterConsole(1));
    }

    @Override
    public void doStack(
            NamedChannelsInput inputObjectUntyped,
            int seriesIndex,
            int numSeries,
            BoundIOContext context)
            throws JobExecutionException {

        try {
            NamedChannelsForSeries channelCollection =
                    createChannelCollection(inputObjectUntyped, seriesIndex);

            ChannelGetter channelGetter = maybeAddFilter(channelCollection, context);

            if (channelConverter != null) {
                channelGetter = maybeAddConverter(channelGetter);
            }

            convertEachTimepoint(
                    seriesIndex,
                    channelCollection.channelNames(),
                    numSeries,
                    channelCollection.sizeT(ProgressReporterNull.get()),
                    channelGetter,
                    context.getLogger());

        } catch (RasterIOException | CreateException | AnchorIOException e) {
            throw new JobExecutionException(e);
        }
    }

    private void convertEachTimepoint(
            int seriesIndex,
            Set<String> channelNames,
            int numSeries,
            int sizeT,
            ChannelGetter channelGetter,
            Logger logger)
            throws AnchorIOException {

        for (int t = 0; t < sizeT; t++) {

            CalculateOutputName namer =
                    new CalculateOutputName(seriesIndex, numSeries, t, sizeT, suppressSeries);

            logger.messageLogger().logFormatted("Starting time-point: %d", t);

            ChannelGetterForTimepoint getterForTimepoint =
                    new ChannelGetterForTimepoint(channelGetter, t);

            channelConversionStyle.convert(
                    channelNames,
                    getterForTimepoint,
                    (name, stack) -> addStackToOutput(name, stack, namer),
                    logger);

            logger.messageLogger().logFormatted("Ending time-point: %d", t);
        }
    }

    private void addStackToOutput(
            String name, Stack stack, CalculateOutputName calculateOutputName) {
        generatorSeq.add(stack, calculateOutputName.outputName(name));
    }

    private ChannelGetter maybeAddConverter(ChannelGetter channelGetter) throws CreateException {
        if (channelConverter != null) {
            return new ConvertingChannels(
                    channelGetter,
                    channelConverter.createConverter(),
                    ConversionPolicy.CHANGE_EXISTING_CHANNEL);
        } else {
            return channelGetter;
        }
    }

    private ChannelGetter maybeAddFilter(
            NamedChannelsForSeries channelCollection, BoundIOContext context) {

        if (channelFilter != null) {

            channelFilter.init((NamedChannelsForSeries) channelCollection, context);
            return channelFilter;
        } else {
            return channelCollection;
        }
    }

    @Override
    public void endSeries(BoundOutputManagerRouteErrors outputManager)
            throws JobExecutionException {
        generatorSeq.end();
    }
}
