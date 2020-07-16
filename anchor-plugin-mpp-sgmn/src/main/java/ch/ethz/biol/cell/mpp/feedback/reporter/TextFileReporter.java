/* (C)2020 */
package ch.ethz.biol.cell.mpp.feedback.reporter;

import java.io.PrintWriter;
import java.util.Optional;
import org.anchoranalysis.anchor.mpp.feature.nrg.cfg.CfgNRGPixelized;
import org.anchoranalysis.io.error.AnchorIOException;
import org.anchoranalysis.io.manifest.ManifestDescription;
import org.anchoranalysis.io.output.file.FileOutput;
import org.anchoranalysis.io.output.file.FileOutputFromManager;
import org.anchoranalysis.mpp.sgmn.bean.optscheme.feedback.ReporterAgg;
import org.anchoranalysis.mpp.sgmn.optscheme.feedback.OptimizationFeedbackEndParams;
import org.anchoranalysis.mpp.sgmn.optscheme.feedback.OptimizationFeedbackInitParams;
import org.anchoranalysis.mpp.sgmn.optscheme.feedback.ReporterException;
import org.anchoranalysis.mpp.sgmn.optscheme.feedback.aggregate.AggregateReceiver;
import org.anchoranalysis.mpp.sgmn.optscheme.feedback.aggregate.Aggregator;
import org.anchoranalysis.mpp.sgmn.optscheme.feedback.aggregate.AggregatorException;
import org.anchoranalysis.mpp.sgmn.optscheme.step.Reporting;
import org.apache.commons.lang.time.StopWatch;

public final class TextFileReporter extends ReporterAgg<CfgNRGPixelized>
        implements AggregateReceiver<CfgNRGPixelized> {

    private Optional<FileOutput> fileOutput;

    private StopWatch timer = null;

    @Override
    public void aggStart(OptimizationFeedbackInitParams<CfgNRGPixelized> initParams, Aggregator agg)
            throws AggregatorException {
        fileOutput =
                FileOutputFromManager.create(
                        "txt",
                        Optional.of(new ManifestDescription("text", "event_log")),
                        initParams.getInitContext().getOutputManager().getDelegate(),
                        "eventLog");
    }

    @Override
    public void reportBegin(OptimizationFeedbackInitParams<CfgNRGPixelized> initParams)
            throws ReporterException {

        super.reportBegin(initParams);
        try {
            if (fileOutput.isPresent()) {
                fileOutput.get().start();

                timer = new StopWatch();
                timer.start();
            }

        } catch (AnchorIOException e) {
            throw new ReporterException(e);
        }
    }

    @Override
    public void aggReport(Reporting<CfgNRGPixelized> reporting, Aggregator agg) {
        if (fileOutput.isPresent() && fileOutput.get().isEnabled()) {

            fileOutput
                    .get()
                    .getWriter()
                    .printf(
                            "itr=%d  time=%e  tpi=%e   %s%n",
                            reporting.getIter(),
                            ((double) timer.getTime()) / 1000,
                            ((double) timer.getTime()) / (reporting.getIter() * 1000),
                            agg.toString());
        }
    }

    @Override
    public void reportNewBest(Reporting<CfgNRGPixelized> reporting) throws ReporterException {
        if (fileOutput.isPresent() && fileOutput.get().isEnabled()) {

            fileOutput
                    .get()
                    .getWriter()
                    .printf(
                            "*** itr=%d  size=%d  best_nrg=%e  kernel=%s%n",
                            reporting.getIter(),
                            reporting.getCfgNRGAfter().getCfg().size(),
                            reporting.getCfgNRGAfter().getCfgNRG().getNrgTotal(),
                            reporting.getKernel().getDescription());
        }
    }

    @Override
    public void aggEnd(Aggregator agg) {
        // NOTHING TO DO
    }

    @Override
    protected AggregateReceiver<CfgNRGPixelized> getAggregateReceiver() {
        return this;
    }

    @Override
    public void reportEnd(OptimizationFeedbackEndParams<CfgNRGPixelized> optStep) {

        super.reportEnd(optStep);
        if (fileOutput.isPresent() && fileOutput.get().isEnabled()) {

            timer.stop();

            PrintWriter writer = this.fileOutput.get().getWriter();
            writer.print(optStep.toString());
            writer.printf("Optimization time took %e s%n", ((double) timer.getTime()) / 1000);
            writer.close();
        }
    }
}
