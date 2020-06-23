package org.anchoranalysis.plugin.mpp.experiment.bean.feature;

/*-
 * #%L
 * anchor-plugin-mpp-experiment
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

import java.util.List;
import java.util.function.BiConsumer;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.core.log.LogErrorReporter;
import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.feature.calc.results.ResultsVector;
import org.anchoranalysis.feature.input.FeatureInput;
import org.anchoranalysis.image.feature.session.FeatureTableSession;

class FeatureCalculator {
	
	/**
	 * Calculates a bunch of features with an objectID (unique) and a groupID and adds them to the stored-results
	 * 
	 * The stored-results also have an additional first-column with the ID.
	 * 
	 * @param session for calculating features
	 * @param listInputs 	a list of parameters. Each parameters creates a new result (e.g. a new row in a feature-table)
	 * @param resultsConsumer called with the results
	 * @param extractIdentifier extracts an identifier from each object that is calculated
	 * @param suppressErrors iff TRUE no exceptions are thrown when an error occurs, but rather a message is written to the log
	 * @param logger the log
	 * @throws OperationFailedException
	 */
	public static <T extends FeatureInput> void calculateManyFeaturesInto(
		FeatureTableSession<T> session,
		List<T> listInputs,
		BiConsumer<ResultsVector,String> resultsConsumer,
		boolean suppressErrors,
		LogErrorReporter logger
	) throws OperationFailedException {

		try {
			for(int i=0; i<listInputs.size(); i++ ) {
				
				T input = listInputs.get(i);
			
				logger.getLogReporter().logFormatted("Calculating input %d of %d: %s", i+1, listInputs.size(), input.toString() );
				
				ResultsVector rv = suppressErrors ? session.calcSuppressErrors(input, logger.getErrorReporter()) : session.calc(input);
				resultsConsumer.accept(
					rv,
					session.uniqueIdentifierFor(input)
				);
			}
			
		} catch (FeatureCalcException e) {
			throw new OperationFailedException(e);
		}
	}
}
