package org.anchoranalysis.plugin.mpp.sgmn.bean.cfgnrg.transformer;

/*-
 * #%L
 * anchor-plugin-mpp-sgmn
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

import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.mpp.sgmn.cfgnrg.transformer.StateTransformerBean;
import org.anchoranalysis.plugin.mpp.sgmn.optscheme.DualCfgNRGPixelized;
import org.anchoranalysis.plugin.mpp.sgmn.optscheme.TransformationContext;

import ch.ethz.biol.cell.mpp.nrg.CfgNRGPixelized;

public class RetrieveDestinationFromDual extends StateTransformerBean<DualCfgNRGPixelized,CfgNRGPixelized> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Override
	public CfgNRGPixelized transform(DualCfgNRGPixelized in, TransformationContext context)
			throws OperationFailedException {
		return in.getDest();
	}

}
