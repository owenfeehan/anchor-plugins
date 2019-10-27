package ch.ethz.biol.cell.countchrom.experiment;

import java.io.IOException;

/*
 * #%L
 * anchor-plugin-mpp-experiment
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


import java.nio.file.Path;

import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.index.GetOperationFailedException;
import org.anchoranalysis.io.csv.reader.CSVReaderByLine;
import org.anchoranalysis.io.csv.reader.CSVReaderByLine.ReadByLine;

import ch.ethz.biol.cell.countchrom.experiment.exportobjectsfromcsv.IndexedCSVRows;
import ch.ethz.biol.cell.countchrom.experiment.exportobjectsfromcsv.columndefinition.ColumnDefinition;

// Mantains an index of the CSV rows, that is initialized first time it's requested
// Assumes the CSV input file path will be the same for all the images. If not throws an error.
public class ExportObjectsFromCSVTaskSharedState {
	
	/**
	 * An index of the rows in the CSV file, that is cached between different threads
	 */
	private IndexedCSVRows indexedRows = null;
	
	/**
	 * We record the csvFilePath just to check to make sure it's always the same, or otherwise throw an error
	 */
	private Path csvFilePathRead;
	
	public IndexedCSVRows getIndexedRowsOrCreate( Path csvFilePath, ColumnDefinition columnDefinition ) throws GetOperationFailedException {
		
		if (indexedRows==null) {
			
			try (ReadByLine reader = CSVReaderByLine.open(csvFilePath)) {
				// Read in our CSV file, and group the rows accordingly
				indexedRows = new IndexedCSVRows(
					reader,
					columnDefinition
				);
				
				this.csvFilePathRead = csvFilePath;
				
			} catch (CreateException | IOException e) {
				throw new GetOperationFailedException(e);
			}
		}
		
		if (!csvFilePath.equals(csvFilePathRead)) {
			throw new GetOperationFailedException(
				String.format(
					"A previous thread read the CSV file '%s', but this thread looks for '%s'. These files must be identical for all inputs",
					csvFilePathRead,
					csvFilePath
				)
			);
		}
		
		return indexedRows;
	}
	
}