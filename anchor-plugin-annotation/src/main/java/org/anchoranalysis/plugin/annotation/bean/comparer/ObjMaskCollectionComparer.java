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
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.image.extent.ImageDim;
import org.anchoranalysis.image.io.objs.ObjMaskCollectionReader;
import org.anchoranalysis.image.objmask.ObjMaskCollection;
import org.anchoranalysis.io.bean.filepath.generator.FilePathGenerator;
import org.anchoranalysis.io.deserializer.DeserializationFailedException;

public class ObjMaskCollectionComparer extends Comparer {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	// START BEAN PROPERTIES
	@BeanField
	private FilePathGenerator filePathGenerator;
	// END BEAN PROPERTIES

	public ObjMaskCollectionComparer() {
		super();
	}
	
	@Override
	public ObjMaskCollection createObjs(Path filePathSource, ImageDim dim, boolean debugMode) throws CreateException {
		
		try {
			Path objsPath = filePathGenerator.outFilePath(filePathSource, debugMode);
			
			if (!Files.exists(objsPath)) {
				return null;
			}
			
			return ObjMaskCollectionReader.createFromPath(objsPath);
			
		} catch (IOException | DeserializationFailedException e) {
			throw new CreateException(e);
		}
	}
	
	public FilePathGenerator getFilePathGenerator() {
		return filePathGenerator;
	}

	public void setFilePathGenerator(FilePathGenerator filePathGenerator) {
		this.filePathGenerator = filePathGenerator;
	}
}
