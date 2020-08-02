/*-
 * #%L
 * anchor-plugin-mpp-sgmn
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

package ch.ethz.biol.cell.mpp.feedback.reporter;

import java.util.Optional;
import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.anchor.mpp.feature.nrg.cfg.CfgNRG;
import org.anchoranalysis.anchor.mpp.feature.nrg.cfg.CfgNRGPixelized;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.io.generator.IterableGenerator;
import org.anchoranalysis.io.generator.sequence.GeneratorSequenceNonIncrementalWriter;
import org.anchoranalysis.io.generator.serialized.BundledObjectOutputStreamGenerator;
import org.anchoranalysis.io.manifest.ManifestDescription;
import org.anchoranalysis.io.manifest.deserializer.bundle.BundleParameters;
import org.anchoranalysis.io.manifest.sequencetype.ChangeSequenceType;
import org.anchoranalysis.io.namestyle.IndexableOutputNameStyle;
import org.anchoranalysis.io.namestyle.IntegerSuffixOutputNameStyle;
import org.anchoranalysis.io.output.bound.BoundOutputManagerRouteErrors;
import org.anchoranalysis.io.output.error.OutputWriteFailedException;
import org.anchoranalysis.mpp.sgmn.bean.optscheme.feedback.FeedbackReceiverBean;
import org.anchoranalysis.mpp.sgmn.optscheme.feedback.OptimizationFeedbackEndParams;
import org.anchoranalysis.mpp.sgmn.optscheme.feedback.OptimizationFeedbackInitParams;
import org.anchoranalysis.mpp.sgmn.optscheme.feedback.ReporterException;
import org.anchoranalysis.mpp.sgmn.optscheme.step.Reporting;

public class CfgNRGSerializerChangeReporter extends FeedbackReceiverBean<CfgNRGPixelized> {

    // START BEAN PARAMETERS
    @BeanField @Getter @Setter private String manifestFunction = "cfgNRG";

    @BeanField @Getter @Setter private String outputName;

    @BeanField @Getter @Setter private int bundleSize = 1000;

    @BeanField @Getter @Setter private boolean best = false;
    // END BEAN PARAMETERS

    private GeneratorSequenceNonIncrementalWriter<CfgNRG> sequenceWriter;

    private ChangeSequenceType sequenceType;

    private Reporting<CfgNRGPixelized> lastOptimizationStep;

    @Override
    public void reportBegin(OptimizationFeedbackInitParams<CfgNRGPixelized> initParams)
            throws ReporterException {

        BoundOutputManagerRouteErrors outputManager =
                initParams.getInitContext().getOutputManager();

        sequenceType = new ChangeSequenceType();

        BundleParameters bundleParams = createBundleParameters();

        IterableGenerator<CfgNRG> iterableGenerator =
                new BundledObjectOutputStreamGenerator<>(
                        bundleParams,
                        this.generateOutputNameStyle(),
                        outputManager.getDelegate(),
                        manifestFunction);

        IndexableOutputNameStyle outputNameStyle = generateOutputNameStyle();
        sequenceWriter =
                new GeneratorSequenceNonIncrementalWriter<>(
                        outputManager.getDelegate(),
                        outputNameStyle.getOutputName(),
                        outputNameStyle,
                        iterableGenerator,
                        true,
                        new ManifestDescription("serialized", manifestFunction));

        try {
            sequenceWriter.start(sequenceType, -1);
        } catch (OutputWriteFailedException e) {
            throw new ReporterException(e);
        }
    }

    @Override
    public void reportItr(Reporting<CfgNRGPixelized> reporting) throws ReporterException {
        try {
            if (reporting.isAccptd() && sequenceWriter != null) {
                addToSequenceWriter(
                        best ? reporting.getBest() : reporting.getCfgNRGAfterOptional(),
                        reporting.getIter());
            }
            lastOptimizationStep = reporting;
        } catch (OutputWriteFailedException e) {
            throw new ReporterException(e);
        }
    }

    @Override
    public void reportEnd(OptimizationFeedbackEndParams<CfgNRGPixelized> optStep)
            throws ReporterException {

        if (sequenceWriter == null) {
            return;
        }

        if (sequenceWriter.isOn() && lastOptimizationStep != null) {
            sequenceType.setMaximumIndex(lastOptimizationStep.getIter());
        }

        try {
            sequenceWriter.end();
        } catch (OutputWriteFailedException e) {
            throw new ReporterException(e);
        }
    }

    @Override
    public void reportNewBest(Reporting<CfgNRGPixelized> reporting) {
        // NOTHING TO DO
    }

    // We generate an OutputName class from the outputName string
    private IndexableOutputNameStyle generateOutputNameStyle() {
        return new IntegerSuffixOutputNameStyle(outputName, 10);
    }

    private BundleParameters createBundleParameters() {
        BundleParameters bundleParams = new BundleParameters();
        bundleParams.setBundleSize(bundleSize);
        bundleParams.setSequenceType(sequenceType);
        return bundleParams;
    }

    private void addToSequenceWriter(Optional<CfgNRGPixelized> cfgNRGAfter, int iter)
            throws OutputWriteFailedException {
        if (cfgNRGAfter.isPresent()) {
            sequenceWriter.add(cfgNRGAfter.get().getCfgNRG(), String.valueOf(iter));
        }
    }
}
