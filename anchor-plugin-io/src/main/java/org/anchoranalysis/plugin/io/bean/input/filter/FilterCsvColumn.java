package org.anchoranalysis.plugin.io.bean.input.filter;

/*-
 * #%L
 * anchor-io
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

import java.nio.file.Path;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.io.bean.filepath.generator.FilePathGenerator;
import org.anchoranalysis.io.bean.input.InputManager;
import org.anchoranalysis.io.bean.input.InputManagerParams;
import org.anchoranalysis.io.csv.reader.CSVReaderException;
import org.anchoranalysis.io.error.AnchorIOException;
import org.anchoranalysis.io.input.InputFromManager;

/**
 * Finds a CSV file with the descriptiveNames of an input as the first-column
 * 
 * Filters against the value of the second-column of the CSV so that only inputs that
 *   match this value are accepted.
 *   
 * Each entry from the delegated input-manager must map 1 to 1 to rows in the CSV file
 * 
 * @author Owen Feehan
 *
 * @param <T> InputType
 */
public class FilterCsvColumn<T extends InputFromManager> extends InputManager<T> {

	// START BEAN PROPERTIES
	@BeanField
	private InputManager<T> input;
	
	@BeanField
	private FilePathGenerator csvFilePath;
	
	@BeanField
	private String match;
	// END BEAN PROPERTIES
	
	@Override
	public List<T> inputObjects(InputManagerParams params)
			throws AnchorIOException {

		// Existing collection 
		List<T> in = input.inputObjects(params);
		
		if (in.size()==0) {
			return in;
		}

		Set<String> matching = matchingNames(
			in.get(0).pathForBindingRequired(),
			params.isDebugModeActivated(),
			in.size()
		);

		applyFilter(in, matching);
		return in;
	}
	
	private Set<String> matchingNames( Path pathForGenerator, boolean doDebug, int numRowsExpected ) throws AnchorIOException {
		// Read CSV file using the path of the first object
		Path csvPath = csvFilePath.outFilePath(pathForGenerator, doDebug );
		
		try {
			return CsvMatcher.rowsFromCsvThatMatch(csvPath, match, numRowsExpected);
		} catch (CSVReaderException e) {
			throw new AnchorIOException("Cannot match rows from csv", e);
		}
	}
	
	// Removes all items from the inputList whose descriptiveName is NOT found in mustContain
	private void applyFilter( List<T> in, Set<String> mustContain ) {
		
		ListIterator<T> itr = in.listIterator();
		while(itr.hasNext()) {
			T item = itr.next();
			
			if (!mustContain.contains(item.descriptiveName())) {
				itr.remove();
			}
		}
	}

	public InputManager<T> getInput() {
		return input;
	}

	public void setInput(InputManager<T> input) {
		this.input = input;
	}

	public String getMatch() {
		return match;
	}

	public void setMatch(String match) {
		this.match = match;
	}

	public FilePathGenerator getCsvFilePath() {
		return csvFilePath;
	}

	public void setCsvFilePath(FilePathGenerator csvFilePath) {
		this.csvFilePath = csvFilePath;
	}

}
