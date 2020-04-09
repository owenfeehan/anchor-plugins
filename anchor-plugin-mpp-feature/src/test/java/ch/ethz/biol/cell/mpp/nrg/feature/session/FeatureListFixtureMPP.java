package ch.ethz.biol.cell.mpp.nrg.feature.session;

import org.anchoranalysis.anchor.mpp.feature.bean.cfg.FeatureCfgParams;
import org.anchoranalysis.anchor.mpp.feature.bean.mark.FeatureMarkParams;

/*-
 * #%L
 * anchor-plugin-mpp-feature
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

import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.feature.bean.list.FeatureList;
import org.anchoranalysis.test.TestLoader;
import org.anchoranalysis.test.feature.plugins.FeatureListFixture;

public class FeatureListFixtureMPP {

	private static TestLoader loader = TestLoader.createFromMavenWorkingDir();
	
	public static FeatureList<FeatureMarkParams> mark() throws CreateException {
		return FeatureListFixture.createFromFile("markFeatureList.xml", loader);
	}
	
	public static FeatureList<FeatureCfgParams> cfg() throws CreateException {
		return FeatureListFixture.createFromFile("cfgFeatureList.xml", loader);
	}

}
