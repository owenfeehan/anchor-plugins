/*-
 * #%L
 * anchor-plugin-mpp-feature
 * %%
 * Copyright (C) 2010 - 2020 Owen Feehan, ETH Zurich, University of Zurich, Hoffmann-La Roche
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

package org.anchoranalysis.plugin.mpp.feature.bean.memo.all;

import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.feature.bean.Feature;
import org.anchoranalysis.feature.calculate.FeatureCalculationException;
import org.anchoranalysis.feature.calculate.FeatureCalculationInput;
import org.anchoranalysis.feature.calculate.cache.ChildCacheName;
import org.anchoranalysis.mpp.feature.bean.energy.element.FeatureAllMemo;
import org.anchoranalysis.mpp.feature.input.FeatureInputAllMemo;
import org.anchoranalysis.mpp.feature.input.FeatureInputSingleMemo;
import org.anchoranalysis.mpp.feature.mark.EnergyMemoList;

public class CoefficientOfVarianceFromAll extends FeatureAllMemo {

    // START BEAN PROPERTIES
    @BeanField @Getter @Setter private Feature<FeatureInputSingleMemo> item;
    // END BEAN PROPERTIES

    @Override
    public double calculate(FeatureCalculationInput<FeatureInputAllMemo> input)
            throws FeatureCalculationException {

        EnergyMemoList memoMarks = input.get().getPxlPartMemo();

        if (memoMarks.size() == 0) {
            return 0.0;
        }

        return calculateStatistic(input, memoMarks);
    }

    private double calculateStatistic(
            FeatureCalculationInput<FeatureInputAllMemo> input, EnergyMemoList memoMarks)
            throws FeatureCalculationException {

        double[] vals = new double[memoMarks.size()];

        double mean = calculateForEachItem(input, memoMarks, vals);

        if (mean == 0.0) {
            return Double.POSITIVE_INFINITY;
        }

        return stdDev(vals, mean) / mean;
    }

    /** Calculates the feature on each mark separately, populating vals, and returns the mean */
    private double calculateForEachItem(
            FeatureCalculationInput<FeatureInputAllMemo> input,
            EnergyMemoList memoMarks,
            double[] vals)
            throws FeatureCalculationException {

        double sum = 0.0;

        for (int i = 0; i < memoMarks.size(); i++) {

            final int index = i;

            double v =
                    input.forChild()
                            .calculate(
                                    item,
                                    new CalculateDeriveSingleMemoInput(index),
                                    new ChildCacheName(CoefficientOfVarianceFromAll.class, i));

            vals[i] = v;
            sum += v;
        }

        return sum / memoMarks.size();
    }

    private static double stdDev(double[] vals, double mean) {
        double sumSqDiff = 0.0;
        for (int i = 0; i < vals.length; i++) {
            double diff = vals[i] - mean;
            sumSqDiff += Math.pow(diff, 2.0);
        }

        return Math.sqrt(sumSqDiff);
    }
}
