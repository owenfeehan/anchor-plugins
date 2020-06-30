package org.anchoranalysis.plugin.image.task.bean.grouped.histogram;



import java.io.IOException;
import java.util.Optional;

/*
 * #%L
 * anchor-plugin-image-task
 * %%
 * Copyright (C) 2016 ETH Zurich, University of Zurich, Owen Feehan
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


/** Calculates feature on a 'grouped' set of images
 
   1. All files are aggregated into groups
   2. For each image file, a histogram is calculated
   3. The histogram is added to the group histogram
   4. The histograms are written to the filesystem
   
**/
public class GroupedHistogramExportTask extends GroupedStackTask<Histogram,Histogram> {

	// START BEAN PROPERTIES
	@BeanField
	private boolean writeImageHistograms = true;	// If enabled writes a histogram for each image, as well as the group
	
	@BeanField @OptionalBean
	private ObjectCollectionProvider objs;		// Optional
	
	/** If defined, this stack is used as a mask over the values which are fed into the histogram */
	@BeanField @AllowEmpty
	private String keyMask = "";
	
	/** What pixel value to read as "On" in the mask above */
	@BeanField
	private int maskValue = 255;
	
	@BeanField
	private boolean csvIgnoreZeros = false;
	// END BEAN PROPERTIES

	@Override
	protected GroupMapByName<Histogram, Histogram> createGroupMap(ConsistentChannelChecker chnlChecker) {
		return new GroupedHistogramMap(
			createWriter(),
			(int) chnlChecker.getMaxValue() 
		);
	}

	@Override
	protected void processKeys(
		NamedImgStackCollection store,
		Optional<String> groupName,
		GroupedSharedState<Histogram,Histogram> sharedState,
		BoundIOContext context
	) throws JobExecutionException {
		
		ChannelSource source = new ChannelSource( store, sharedState.getChnlChecker(), Optional.empty() );
		
		HistogramExtracter histogramExtracter = new HistogramExtracter(
			source,
			keyMask,
			maskValue
		);
			
		try {
			for( NamedChnl chnl : getSelectChnls().selectChnls(source, true)) {
				
				addHistogramFromChnl(
					chnl,
					histogramExtracter,
					groupName,
					sharedState.getGroupMap(),
					context
				);
			}
			
		} catch (OperationFailedException e) {
			throw new JobExecutionException(e);
		}
	}
	
	private void addHistogramFromChnl(
		NamedChnl chnl,
		HistogramExtracter histogramExtracter,
		Optional<String> groupName,
		GroupMapByName<Histogram,Histogram> groupMap,
		BoundIOContext context
	) throws JobExecutionException {
		
		Histogram hist = histogramExtracter.extractFrom(chnl.getChnl());
		
		if (writeImageHistograms) {
			// We keep histogram as private member variable so it is thread-safe
			try {
				createWriter().writeHistogramToFile(
					hist,
					chnl.getName(),
					context
				);
			} catch (IOException e) {
				throw new JobExecutionException(e);
			}
		}
		
		groupMap.add(
			groupName,
			chnl.getName(),
			hist
		);
	}
	
	private GroupedHistogramWriter createWriter() {
		return new GroupedHistogramWriter(csvIgnoreZeros);
	}

	@Override
	protected Optional<String> subdirectoryForGroupOutputs() {
		return Optional.of("sum");
	}

	public boolean isWriteImageHistograms() {
		return writeImageHistograms;
	}

	public void setWriteImageHistograms(boolean writeImageHistograms) {
		this.writeImageHistograms = writeImageHistograms;
	}

	public ObjectCollectionProvider getObjs() {
		return objs;
	}


	public void setObjs(ObjectCollectionProvider objs) {
		this.objs = objs;
	}

	public String getKeyMask() {
		return keyMask;
	}

	public void setKeyMask(String keyMask) {
		this.keyMask = keyMask;
	}

	public int getMaskValue() {
		return maskValue;
	}

	public void setMaskValue(int maskValue) {
		this.maskValue = maskValue;
	}

	public boolean isCsvIgnoreZeros() {
		return csvIgnoreZeros;
	}

	public void setCsvIgnoreZeros(boolean csvIgnoreZeros) {
		this.csvIgnoreZeros = csvIgnoreZeros;
	}
}
