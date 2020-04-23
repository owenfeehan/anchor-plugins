package ch.ethz.biol.cell.mpp.nrg.feature.cfg;

import org.anchoranalysis.anchor.mpp.bean.points.CreateMarkFromPoints;
import org.anchoranalysis.anchor.mpp.feature.bean.nrg.elem.NRGElemAll;
import org.anchoranalysis.anchor.mpp.feature.input.memo.FeatureInputAllMemo;
import org.anchoranalysis.anchor.mpp.feature.input.memo.FeatureInputSingleMemo;
import org.anchoranalysis.anchor.mpp.mark.Mark;
import org.anchoranalysis.anchor.mpp.mark.points.MarkPointList;
import org.anchoranalysis.anchor.mpp.pixelpart.factory.PixelPartFactoryHistogram;
import org.anchoranalysis.anchor.mpp.pxlmark.memo.PxlMarkMemo;

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

import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.feature.bean.Feature;
import org.anchoranalysis.feature.bean.operator.Constant;
import org.anchoranalysis.feature.cache.CacheableParams;
import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.feature.nrg.NRGStackWithParams;

/**
 * Assumes all marks are {@link MarkPointList} and combines the points from these marks
 * Then fits a mark from a PointersFitter
 * And calculates a feature on the fitted shape
 * 
 * @author FEEHANO
 *
 */
public class CombinePointsToMark extends NRGElemAll {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	// START BEAN PROPERTIES
	@BeanField
	private CreateMarkFromPoints createMark;
	
	@BeanField
	private Feature<FeatureInputSingleMemo> item;
	
	@BeanField
	private Feature<FeatureInputAllMemo> featureElse = new Constant<>(0);
	// END BEAN PROPERTIES
	
	@Override
	public double calc(CacheableParams<FeatureInputAllMemo> paramsCacheable) throws FeatureCalcException {

		FeatureInputAllMemo params = paramsCacheable.getParams();
		
		try {
			Mark mark = createMark.fitMarkToPointsFromCfg(
				params.getPxlPartMemo().asCfg(),
				params.getDimensions()
			);
			
			if (mark!=null) {
				return calcFeatureOnMarks(mark, paramsCacheable);
			} else {
				return paramsCacheable.calc(featureElse);
			}
			
			
		} catch (OperationFailedException e) {
			throw new FeatureCalcException(e);
		}
	}
		
	private double calcFeatureOnMarks( Mark mark, CacheableParams<FeatureInputAllMemo> paramsCacheable ) throws FeatureCalcException {
		
		return paramsCacheable.calcChangeParams(
			item,
			p -> deriveIndParams(p, mark),
			"ind"
		);
	}
	
	private static FeatureInputSingleMemo deriveIndParams( FeatureInputAllMemo params, Mark mark ) {
		
		NRGStackWithParams nrgStack = params.getNrgStack();
		
		PxlMarkMemo memo = new PxlMarkMemo(
			mark,
			nrgStack.getNrgStack(),
			params.getPxlPartMemo().getRegionMap(),
			new PixelPartFactoryHistogram()
		);
							
		return new FeatureInputSingleMemo(	memo, nrgStack );		
	}
	
	public Feature<FeatureInputAllMemo> getFeatureElse() {
		return featureElse;
	}

	public void setFeatureElse(Feature<FeatureInputAllMemo> featureElse) {
		this.featureElse = featureElse;
	}

	public Feature<FeatureInputSingleMemo> getItem() {
		return item;
	}

	public void setItem(Feature<FeatureInputSingleMemo> item) {
		this.item = item;
	}

	public CreateMarkFromPoints getCreateMark() {
		return createMark;
	}

	public void setCreateMark(CreateMarkFromPoints createMark) {
		this.createMark = createMark;
	}
}
