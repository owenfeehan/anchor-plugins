/* (C)2020 */
package org.anchoranalysis.plugin.io.multifile;

import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.anchoranalysis.core.progress.ProgressReporter;
import org.anchoranalysis.core.progress.ProgressReporterNull;
import org.anchoranalysis.image.extent.ImageDimensions;
import org.anchoranalysis.image.io.RasterIOException;
import org.anchoranalysis.image.io.bean.rasterreader.RasterReader;
import org.anchoranalysis.image.io.rasterreader.OpenedRaster;
import org.anchoranalysis.image.stack.TimeSequence;

// Ignores multiple series
@RequiredArgsConstructor
public class MultiFileReaderOpenedRaster extends OpenedRaster {

    // START REQUIRED ARGUMENTS
    private final RasterReader rasterReader;
    private final ParsedFilePathBag fileBag;
    // END REQUIRED ARGUMENTS

    // Processed version of the file. If null, not set yet
    private MultiFile multiFileMemo = null;

    @Override
    public int numSeries() {
        // For now we only support a single series, this could be changed
        return 1;
    }

    @Override
    public TimeSequence open(int seriesIndex, ProgressReporter progressReporter)
            throws RasterIOException {

        try {
            progressReporter.open();
            return getOrCreateMemo(progressReporter).createSequence();

        } finally {
            progressReporter.close();
        }
    }

    @Override
    public Optional<List<String>> channelNames() {
        return Optional.empty();
    }

    @Override
    public int numChnl() throws RasterIOException {
        MultiFile memo = getOrCreateMemo(ProgressReporterNull.get());

        if (!memo.numChnlDefined()) {
            throw new RasterIOException("Number of chnl is not defined");
        }

        return memo.numChnl();
    }

    @Override
    public int bitDepth() throws RasterIOException {
        MultiFile memo = getOrCreateMemo(ProgressReporterNull.get());
        return memo.dataType().numBits();
    }

    @Override
    public boolean isRGB() throws RasterIOException {
        return false;
    }

    @Override
    public int numFrames() throws RasterIOException {

        MultiFile multiFile = getOrCreateMemo(ProgressReporterNull.get());

        if (!multiFile.numFramesDefined()) {
            throw new RasterIOException("Number of frames is not defined");
        }

        return multiFile.numFrames();
    }

    @Override
    public void close() throws RasterIOException {
        // NOTHING TO DO
    }

    @Override
    public ImageDimensions dim(int seriesIndex) throws RasterIOException {
        throw new RasterIOException("MultiFileReader doesn't support this operation");
    }

    private void addDetailsFromBag(
            MultiFile multiFile, int seriesIndex, ProgressReporter progressReporter)
            throws RasterIOException {

        for (FileDetails fd : fileBag) {

            OpenedRaster or = rasterReader.openFile(fd.getFilePath());
            try {
                TimeSequence ts = or.open(seriesIndex, progressReporter);
                multiFile.add(
                        ts.get(0),
                        fd.getChnlNum(),
                        fd.getSliceNum(),
                        fd.getTimeIndex(),
                        fd.getFilePath());
            } catch (Exception e) {
                throw new RasterIOException(
                        String.format(
                                "Could not open '%s'. Abandoning MultiFile.", fd.getFilePath()),
                        e);
            } finally {
                or.close();
            }
        }
    }

    private MultiFile getOrCreateMemo(ProgressReporter progressReporter) throws RasterIOException {
        if (multiFileMemo == null) {
            multiFileMemo = new MultiFile(fileBag);
            addDetailsFromBag(multiFileMemo, 0, progressReporter);
        }
        return multiFileMemo;
    }
}
