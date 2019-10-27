package org.anchoranalysis.plugin.annotation.bean.comparer;

/*
 * #%L
 * anchor-annotation
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


import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.anchoranalysis.annotation.io.bean.comparer.Comparer;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.annotation.DefaultInstance;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.image.binary.BinaryChnl;
import org.anchoranalysis.image.binary.values.BinaryValues;
import org.anchoranalysis.image.extent.ImageDim;
import org.anchoranalysis.image.io.RasterIOException;
import org.anchoranalysis.image.io.bean.rasterreader.RasterReader;
import org.anchoranalysis.image.io.bean.rasterreader.RasterReaderUtilities;
import org.anchoranalysis.image.objmask.ObjMask;
import org.anchoranalysis.image.objmask.ObjMaskCollection;
import org.anchoranalysis.io.bean.filepath.generator.FilePathGenerator;

public class BinaryChnlComparer extends Comparer {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	// START BEAN PROPERTIES
	@BeanField
	private FilePathGenerator filePathGenerator;
	
	@BeanField @DefaultInstance
	private RasterReader rasterReader;
	
	@BeanField
	private boolean invert=false;
	// END BEAN PROPERTIES

	public BinaryChnlComparer() {
		super();
	}
	
	@Override
	public ObjMaskCollection createObjs(Path filePathSource, ImageDim dim, boolean debugMode) throws CreateException {
		
		try {
			Path maskPath = filePathGenerator.outFilePath(filePathSource, debugMode);
			
			if (!Files.exists(maskPath)) {
				return null;
			}
			
			BinaryChnl chnl = RasterReaderUtilities.openBinaryChnl(
				rasterReader,
				maskPath,
				createBinaryValues()
			);
			
			return convertToObjs( chnl );
			
		} catch (IOException | RasterIOException e) {
			throw new CreateException(e);
		}
	}
	
	private BinaryValues createBinaryValues() {
		if (invert) {
			return BinaryValues.getDefault().createInverted();
		} else {
			return BinaryValues.getDefault();
		}
	}
	
	private static ObjMaskCollection convertToObjs( BinaryChnl chnl ) throws CreateException {
		return new ObjMaskCollection(
			new ObjMask( chnl.binaryVoxelBox() )
		);
	}
	
	public FilePathGenerator getFilePathGenerator() {
		return filePathGenerator;
	}

	public void setFilePathGenerator(FilePathGenerator filePathGenerator) {
		this.filePathGenerator = filePathGenerator;
	}

	public RasterReader getRasterReader() {
		return rasterReader;
	}

	public void setRasterReader(RasterReader rasterReader) {
		this.rasterReader = rasterReader;
	}

	public boolean isInvert() {
		return invert;
	}

	public void setInvert(boolean invert) {
		this.invert = invert;
	}
}
