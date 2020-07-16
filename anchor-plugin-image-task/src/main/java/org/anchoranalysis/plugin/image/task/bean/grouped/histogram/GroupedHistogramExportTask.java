/* (C)2020 */
package org.anchoranalysis.plugin.image.task.bean.grouped.histogram;

import java.util.Optional;
import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.AllowEmpty;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.annotation.OptionalBean;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.experiment.JobExecutionException;
import org.anchoranalysis.image.bean.provider.ObjectCollectionProvider;
import org.anchoranalysis.image.histogram.Histogram;
import org.anchoranalysis.image.stack.NamedImgStackCollection;
import org.anchoranalysis.io.output.bound.BoundIOContext;
import org.anchoranalysis.plugin.image.task.bean.grouped.GroupedStackTask;
import org.anchoranalysis.plugin.image.task.grouped.ChannelSource;
import org.anchoranalysis.plugin.image.task.grouped.ConsistentChannelChecker;
import org.anchoranalysis.plugin.image.task.grouped.GroupMapByName;
import org.anchoranalysis.plugin.image.task.grouped.GroupedSharedState;
import org.anchoranalysis.plugin.image.task.grouped.NamedChnl;

/**
 * Calculates feature on a 'grouped' set of images
 *
 * <p>1. All files are aggregated into groups 2. For each image file, a histogram is calculated 3.
 * The histogram is added to the group histogram 4. The histograms are written to the filesystem
 */
public class GroupedHistogramExportTask extends GroupedStackTask<Histogram, Histogram> {

    // START BEAN PROPERTIES
    @BeanField @Getter @Setter
    private boolean writeImageHistograms =
            true; // If enabled writes a histogram for each image, as well as the group

    @BeanField @OptionalBean @Getter @Setter private ObjectCollectionProvider objects; // Optional

    /** If defined, this stack is used as a mask over the values which are fed into the histogram */
    @BeanField @AllowEmpty @Getter @Setter private String keyMask = "";

    /** What pixel value to read as "On" in the mask above */
    @BeanField @Getter @Setter private int maskValue = 255;

    @BeanField @Getter @Setter private boolean csvIgnoreZeros = false;
    // END BEAN PROPERTIES

    @Override
    protected GroupMapByName<Histogram, Histogram> createGroupMap(
            ConsistentChannelChecker chnlChecker) {
        return new GroupedHistogramMap(createWriter(), (int) chnlChecker.getMaxValue());
    }

    @Override
    protected void processKeys(
            NamedImgStackCollection store,
            Optional<String> groupName,
            GroupedSharedState<Histogram, Histogram> sharedState,
            BoundIOContext context)
            throws JobExecutionException {

        ChannelSource source =
                new ChannelSource(store, sharedState.getChnlChecker(), Optional.empty());

        HistogramExtracter histogramExtracter = new HistogramExtracter(source, keyMask, maskValue);

        try {
            for (NamedChnl chnl : getSelectChnls().selectChnls(source, true)) {

                addHistogramFromChnl(
                        chnl, histogramExtracter, groupName, sharedState.getGroupMap(), context);
            }

        } catch (OperationFailedException e) {
            throw new JobExecutionException(e);
        }
    }

    private void addHistogramFromChnl(
            NamedChnl chnl,
            HistogramExtracter histogramExtracter,
            Optional<String> groupName,
            GroupMapByName<Histogram, Histogram> groupMap,
            BoundIOContext context)
            throws JobExecutionException {

        Histogram hist = histogramExtracter.extractFrom(chnl.getChnl());

        if (writeImageHistograms) {
            // We keep histogram as private member variable so it is thread-safe
            createWriter().writeHistogramToFile(hist, chnl.getName(), context);
        }

        groupMap.add(groupName, chnl.getName(), hist);
    }

    private GroupedHistogramWriter createWriter() {
        return new GroupedHistogramWriter(csvIgnoreZeros);
    }

    @Override
    protected Optional<String> subdirectoryForGroupOutputs() {
        return Optional.of("sum");
    }
}
