package ch.ethz.biol.cell.mpp.nrg.feature.ind;

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

import org.anchoranalysis.feature.bean.operator.FeatureSingleElem;
import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.feature.calc.params.FeatureCalcParams;
import org.anchoranalysis.feature.params.FeatureParamsDescriptor;

import ch.ethz.biol.cell.mpp.nrg.NRGElemIndCalcParams;
import ch.ethz.biol.cell.mpp.nrg.NRGElemIndCalcParamsDescriptor;
import ch.ethz.biol.cell.mpp.nrg.feature.mark.FeatureMarkParams;

public class AsMark extends FeatureSingleElem {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Override
	public double calc(FeatureCalcParams params) throws FeatureCalcException {
		
		if (params instanceof NRGElemIndCalcParams) {
		
			NRGElemIndCalcParams paramsCast = (NRGElemIndCalcParams) params; 
			
			FeatureMarkParams paramsNew = new FeatureMarkParams(
				paramsCast.getPxlPartMemo().getMark(),
				paramsCast.getDimensions().getRes()
			);
			
			return getCacheSession().calc( getItem(), paramsNew);
			
		} else {
			throw new FeatureCalcException("Not supported for this type of params");
		}
	}

	// We change the default behaviour, as we don't want to give the same paramsFactory
	//   as the item we pass to
	@Override
	public FeatureParamsDescriptor paramType()
			throws FeatureCalcException {
		return NRGElemIndCalcParamsDescriptor.instance;
	}

	@Override
	public String getParamDscr() {
		return getItem().getParamDscr();
	}


}
