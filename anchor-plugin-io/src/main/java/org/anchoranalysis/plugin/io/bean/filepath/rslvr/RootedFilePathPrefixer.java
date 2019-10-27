package org.anchoranalysis.plugin.io.bean.filepath.rslvr;

/*
 * #%L
 * anchor-io
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
import java.nio.file.Path;

import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.io.bean.filepath.prefixer.FilePathPrefixer;
import org.anchoranalysis.io.bean.root.RootPathMap;
import org.anchoranalysis.io.filepath.prefixer.FilePathPrefix;
import org.apache.log4j.Logger;

public class RootedFilePathPrefixer extends FilePathPrefixer {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	// START BEAN PROPERTIES
	@BeanField
	private FilePathPrefixerAvoidResolve filePathPrefixer;
	
	// The name of the RootPath to associate with this fileset
	@BeanField
	private String rootName;
	// END BEAN PROPERTIES

	private static Logger logger = Logger.getLogger(RootedFilePathPrefixer.class);
		
	@Override
	public FilePathPrefix outFilePrefix(Path pathIn, String expName, boolean debugMode)
			throws IOException {

		logger.debug( String.format("pathIn=%s", pathIn) );
		
		Path pathInWithoutRoot = RootPathMap.instance().split(pathIn, rootName, debugMode).getPath();
		
		FilePathPrefix fpp = filePathPrefixer.outFilePrefixAvoidResolve( pathInWithoutRoot, expName);
		
		Path pathOut = folderPathOut(fpp.getFolderPath(), debugMode);
		fpp.setFolderPath( pathOut );
		
		logger.debug( String.format("prefix=%s", fpp.getFolderPath()) );
		logger.debug( String.format("multiRoot+Rest()=%s",pathOut) );
		
		return fpp;
	}
	
	private Path folderPathOut( Path pathIn, boolean debugMode ) throws IOException {
		return RootPathMap.instance().findRoot(rootName, debugMode).asPath().resolve( pathIn );
	}

	@Override
	public FilePathPrefix rootFolderPrefix(String expName, boolean debugMode) throws IOException {
		FilePathPrefix fpp = filePathPrefixer.rootFolderPrefixAvoidResolve(expName) ;
		

		fpp.setFolderPath( folderPathOut( fpp.getFolderPath(), debugMode ) );
		return fpp;
	}

	public FilePathPrefixerAvoidResolve getFilePathPrefixer() {
		return filePathPrefixer;
	}

	public void setFilePathPrefixer(FilePathPrefixerAvoidResolve filePathPrefixer) {
		this.filePathPrefixer = filePathPrefixer;
	}

	public String getRootName() {
		return rootName;
	}

	public void setRootName(String rootName) {
		this.rootName = rootName;
	}

}
