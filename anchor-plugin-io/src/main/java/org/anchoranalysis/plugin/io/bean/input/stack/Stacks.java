package org.anchoranalysis.plugin.io.bean.input.stack;

/*
 * #%L
 * anchor-image-io
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


import java.util.List;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.annotation.DefaultInstance;
import org.anchoranalysis.core.functional.FunctionalUtilities;
import org.anchoranalysis.image.io.bean.rasterreader.RasterReader;
import org.anchoranalysis.io.bean.input.InputManager;
import org.anchoranalysis.io.bean.input.InputManagerParams;
import org.anchoranalysis.io.error.AnchorIOException;
import org.anchoranalysis.io.input.FileInput;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Each file gives either:
 *   * a single stack
 *   * a time-series of stacks
 *   
 * @author Owen Feehan
 *
 */
@NoArgsConstructor
public class Stacks extends InputManager<StackSequenceInput> {

	// START BEANS
	@BeanField  @Getter @Setter
	private InputManager<FileInput> fileInput;
	
	@BeanField @DefaultInstance @Getter @Setter
	private RasterReader rasterReader;
	
	@BeanField @Getter @Setter
	private boolean useLastSeriesIndexOnly;
	// END BEANS
	
	public Stacks( InputManager<FileInput> fileInput ) {
		this.fileInput = fileInput;
	}

	@Override
	public List<StackSequenceInput> inputObjects(InputManagerParams params)	throws AnchorIOException {
		return FunctionalUtilities.mapToList(
			fileInput.inputObjects(params),
			file-> new StackCollectionFromFilesInputObject(file, getRasterReader(), useLastSeriesIndexOnly)
		);
	}
}
