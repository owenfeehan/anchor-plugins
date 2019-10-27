package org.anchoranalysis.plugin.image.task.bean.feature;

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

import org.anchoranalysis.core.log.LogErrorReporter;
import org.anchoranalysis.core.params.KeyValueParams;
import org.anchoranalysis.feature.calc.ResultsVector;
import org.anchoranalysis.feature.name.FeatureNameList;
import org.anchoranalysis.io.generator.serialized.KeyValueParamsGenerator;
import org.anchoranalysis.io.output.bound.BoundOutputManagerRouteErrors;

/** Exports a ResultVector as a KeyValueParmas */
class KeyValueParamsExporter {

	public static void export( FeatureNameList featureNames, ResultsVector rv, BoundOutputManagerRouteErrors outputManager, LogErrorReporter logErrorReporter ) {
		KeyValueParams kvp = convert( featureNames, rv, logErrorReporter );
		writeKeyValueParams(kvp, outputManager);		
	}
	
	private static void writeKeyValueParams( KeyValueParams kvp, BoundOutputManagerRouteErrors outputManager ) {
		outputManager.getWriterCheckIfAllowed().write(
			"keyValueParams",
			() -> new KeyValueParamsGenerator(kvp, "keyValueParams")
		);		
	}
	
	private static KeyValueParams convert( FeatureNameList featureNames, ResultsVector rv, LogErrorReporter logErrorReporter ) {
		assert(featureNames.size()==rv.length());
		
		KeyValueParams kv = new KeyValueParams();
		for( int i=0; i<featureNames.size(); i++) {
			
			String key = featureNames.get(i);
			Double val = rv.getDoubleOrNull(i);
			
			if (val==null) {
				// Then an error happened and we report it
				logErrorReporter.getErrorReporter().recordError(ExportFeaturesHistogramTask.class, rv.getException(i) );
				val = Double.NaN;
			}
			
			kv.put(key, val);
		}
		return kv;
	}
}
