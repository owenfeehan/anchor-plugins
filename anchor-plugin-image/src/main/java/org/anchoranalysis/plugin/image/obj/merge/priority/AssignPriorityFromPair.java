package org.anchoranalysis.plugin.image.obj.merge.priority;

/*-
 * #%L
 * anchor-plugin-image
 * %%
 * Copyright (C) 2010 - 2020 Owen Feehan
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

import java.util.Optional;

import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.core.error.reporter.ErrorReporter;
import org.anchoranalysis.core.relation.RelationToValue;
import org.anchoranalysis.feature.session.calculator.FeatureCalculatorSingle;
import org.anchoranalysis.image.feature.object.input.FeatureInputPairObjects;
import org.anchoranalysis.image.object.ObjectMask;
import org.anchoranalysis.plugin.image.obj.merge.ObjVertex;

import lombok.AllArgsConstructor;


/**
 * <p>Calculates pair-feature on each potential merge, and this value determines priority.</p>.
 * 
 * @author Owen Feehan
 *
 */
@AllArgsConstructor
public class AssignPriorityFromPair extends AssignPriority {

	private final FeatureCalculatorSingle<FeatureInputPairObjects> featureCalculator;
	private final double threshold;
	private final RelationToValue relation;
	
	@Override
	public PrioritisedVertex assignPriorityToEdge(
		ObjVertex src,
		ObjVertex dest,
		ObjectMask merge,
		ErrorReporter errorReporter
	) throws OperationFailedException {

		double resultPair = featureCalculator.calcSuppressErrors(
			createInput(src, dest, merge),
			errorReporter
		);

		return new PrioritisedVertex(
			merge,
			0,
			resultPair,
			relation.isRelationToValueTrue(resultPair,threshold)
		);
	}
	
	private FeatureInputPairObjects createInput(
		ObjVertex omSrcWithFeature,
		ObjVertex omDestWithFeature,
		ObjectMask omMerge	
	) {
		return new FeatureInputPairObjects(
			omSrcWithFeature.getObjMask(),
			omDestWithFeature.getObjMask(),
			Optional.empty(),
			Optional.of(omMerge)
		);
	}
}
