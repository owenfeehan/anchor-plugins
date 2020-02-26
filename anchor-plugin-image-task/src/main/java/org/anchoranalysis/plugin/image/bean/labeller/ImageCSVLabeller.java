package org.anchoranalysis.plugin.image.bean.labeller;

/*-
 * #%L
 * anchor-plugin-image-task
 * %%
 * Copyright (C) 2010 - 2019 Owen Feehan, ETH Zurich, University of Zurich, Hoffmann la Roche
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

import java.io.IOException;
import java.nio.file.Path;
import java.util.Set;

import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.error.InitException;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.core.log.LogErrorReporter;
import org.anchoranalysis.image.experiment.label.FileLabelMap;
import org.anchoranalysis.image.io.input.ProvidesStackInput;
import org.anchoranalysis.io.bean.filepath.generator.FilePathGenerator;
import org.anchoranalysis.io.error.AnchorIOException;
import org.anchoranalysis.plugin.image.labeller.init.ImageCSVLabellerInitParams;

public class ImageCSVLabeller extends ImageLabeller<ImageCSVLabellerInitParams> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	// START BEAN PROPERTIES
	/** 
	 * Path to a CSV label file (comma-separated, with header, no quotes)
	 *  
	 *  The CSV file should have two columns:
	 *    first column = image id (to match the descriptiveName() of an image)
	 *    second column = a label string   
	 */
	@BeanField
	private FilePathGenerator csvLabelFilePathGenerator;
	// END BEAN PROPERTIES
	
	@Override
	public ImageCSVLabellerInitParams init( Path pathForBinding ) throws InitException {

		try {
			Path csvPath = csvLabelFilePathGenerator.outFilePath(pathForBinding, false);
			
			return new ImageCSVLabellerInitParams(
				FileLabelMap.readFromCSV(csvPath, false)	
			);
			
		} catch (IOException | AnchorIOException e) {
			throw new InitException(e);
		}
	}
	
	@Override
	public Set<String> allLabels(ImageCSVLabellerInitParams params) {
		return params.getLabelMap().labels();
	}

	@Override
	public String labelFor(ImageCSVLabellerInitParams initParams, ProvidesStackInput input, LogErrorReporter logErrorReporter)
			throws OperationFailedException {
		String label = initParams.getLabelMap().get(
			input.descriptiveName()
		);
		
		if (label==null) {
			throw new OperationFailedException(
				String.format("No label can be found for the descriptive-name: %s", input.descriptiveName())
			);
		}
		
		return label;
	}

	public FilePathGenerator getCsvLabelFilePathGenerator() {
		return csvLabelFilePathGenerator;
	}

	public void setCsvLabelFilePathGenerator(FilePathGenerator csvLabelFilePathGenerator) {
		this.csvLabelFilePathGenerator = csvLabelFilePathGenerator;
	}

}
