package org.anchoranalysis.plugin.image.calculation;

import java.util.Optional;

/*
 * #%L
 * anchor-plugin-image
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


import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.feature.cache.calculation.CalculationResolver;
import org.anchoranalysis.feature.cache.calculation.FeatureCalculation;
import org.anchoranalysis.feature.cache.calculation.ResolvedCalculation;
import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.image.extent.ImageDim;
import org.anchoranalysis.image.feature.objmask.FeatureInputSingleObj;
import org.anchoranalysis.image.feature.objmask.pair.FeatureInputPairObjs;
import org.anchoranalysis.image.objmask.ObjMask;
import org.anchoranalysis.image.objmask.morph.MorphologicalErosion;
import org.anchoranalysis.image.objmask.ops.ObjMaskMerger;
import org.anchoranalysis.plugin.image.feature.bean.obj.pair.order.CalculateInputFromPair;
import org.anchoranalysis.plugin.image.feature.bean.obj.pair.order.CalculateInputFromPair.Extract;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

/**
 * Combines two object-masks by:
 *   1. dilating objMask1 by iterations1
 *   2. dilating objMask2 by iterations2
 *   3. finding the intersection of 1. and 2.
 *   4. then eroding by iterationsErosion 
 * 
 *  This is NOT commutative:  f(a,b)==f(b,a)
 * 
 * @author Owen Feehan
 *
 */
public class CalculatePairIntersection extends FeatureCalculation<Optional<ObjMask>,FeatureInputPairObjs> {

	private boolean do3D;
	private int iterationsErosion;
	
	private ResolvedCalculation<ObjMask,FeatureInputSingleObj> calcDilation1;
	private ResolvedCalculation<ObjMask,FeatureInputSingleObj> calcDilation2;
	private ResolvedCalculation<FeatureInputSingleObj, FeatureInputPairObjs> calcLeft;
	private ResolvedCalculation<FeatureInputSingleObj, FeatureInputPairObjs> calcRight;
	
	public static ResolvedCalculation<Optional<ObjMask>,FeatureInputPairObjs> createFromCache(
		CalculationResolver<FeatureInputPairObjs> cache,
		CalculationResolver<FeatureInputSingleObj> cacheDilationObj1,
		CalculationResolver<FeatureInputSingleObj> cacheDilationObj2,
		int iterations1,
		int iterations2,
		boolean do3D,
		int iterationsErosion
	) throws CreateException {
		
		// We use two additional caches, for the calculations involving the single objects, as these can be expensive, and we want
		//  them also cached
		ResolvedCalculation<ObjMask,FeatureInputSingleObj> ccDilation1 = CalculateDilation.createFromCache(
			cacheDilationObj1, iterations1, do3D	
		);
		ResolvedCalculation<ObjMask,FeatureInputSingleObj> ccDilation2 = CalculateDilation.createFromCache(
			cacheDilationObj2, iterations2, do3D	
		);
		return cache.search(
			new CalculatePairIntersection(
				do3D,
				iterationsErosion,
				cache.search( new CalculateInputFromPair(Extract.FIRST) ),
				cache.search( new CalculateInputFromPair(Extract.SECOND) ),
				ccDilation1,
				ccDilation2
			)
		);
	}
		
	private CalculatePairIntersection(
		boolean do3D,
		int iterationsErosion,
		ResolvedCalculation<FeatureInputSingleObj, FeatureInputPairObjs> calcLeft,
		ResolvedCalculation<FeatureInputSingleObj, FeatureInputPairObjs> calcRight,
		ResolvedCalculation<ObjMask,FeatureInputSingleObj> calcDilationLeft,
		ResolvedCalculation<ObjMask,FeatureInputSingleObj> calcDilationRight
	) {
		super();
		this.iterationsErosion = iterationsErosion;
		this.do3D = do3D;
		this.calcDilation1 = calcDilationLeft;
		this.calcDilation2 = calcDilationRight;
		this.calcLeft = calcLeft;
		this.calcRight = calcRight;
	}

	@Override
	protected Optional<ObjMask> execute( FeatureInputPairObjs input ) throws FeatureCalcException {
	
		ImageDim dim = input.getDimensionsRequired();
				
		ObjMask om1Dilated = dilatedObjMask(input, calcLeft, calcDilation1);
		ObjMask om2Dilated = dilatedObjMask(input, calcRight, calcDilation2);
		
		Optional<ObjMask> omIntersection  = om1Dilated.intersect(om2Dilated, dim );
				
		if (!omIntersection.isPresent()) {
			return Optional.empty();
		}
		
		assert( omIntersection.get().hasPixelsGreaterThan(0) );
		
		try {
			if (iterationsErosion>0) {
				return erode(input, omIntersection.get(), dim);
			} else {
				return omIntersection;
			}
			
		} catch (CreateException e) {
			throw new FeatureCalcException(e);
		}
	}

	@Override
	public boolean equals(final Object obj){
	    if(obj instanceof CalculatePairIntersection){
	        final CalculatePairIntersection other = (CalculatePairIntersection) obj;
	        return new EqualsBuilder()
	            .append(iterationsErosion, other.iterationsErosion)
	            .append(do3D, other.do3D)
	            .append(calcDilation1, other.calcDilation1)
	            .append(calcDilation2, other.calcDilation2)
	            .isEquals();
	    } else{
	        return false;
	    }
	}
	
	@Override
	public int hashCode() {
		return new HashCodeBuilder()
			.append(iterationsErosion)
			.append(do3D)
			.append( calcDilation1 )
			.append( calcDilation2 )
			.toHashCode();
	}
	
	private Optional<ObjMask> erode( FeatureInputPairObjs params, ObjMask omIntersection, ImageDim dim ) throws CreateException {

		ObjMask omMerged = params.getMerged();
		
		// We merge the two masks, and then erode it, and use this as a mask on the input object
		if (omMerged==null) {
			omMerged = ObjMaskMerger.merge( params.getFirst(), params.getSecond() );
		}
		
		ObjMask omMergedEroded = MorphologicalErosion.createErodedObjMask(
			omMerged,
			dim.getExtnt(),
			do3D,
			iterationsErosion,
			true,
			null
		);
		
		Optional<ObjMask> omIntersect = omIntersection.intersect(omMergedEroded, dim);
		omIntersect.ifPresent( om-> { assert( om.hasPixelsGreaterThan(0) ); });
		return omIntersect;
	}
	
	private static ObjMask dilatedObjMask(
		FeatureInputPairObjs input,
		ResolvedCalculation<FeatureInputSingleObj, FeatureInputPairObjs> calcSingle,
		ResolvedCalculation<ObjMask,FeatureInputSingleObj> calcDilation
	) throws FeatureCalcException {
		return calcDilation.getOrCalculate(
			calcSingle.getOrCalculate(input)
		);
	}
}
