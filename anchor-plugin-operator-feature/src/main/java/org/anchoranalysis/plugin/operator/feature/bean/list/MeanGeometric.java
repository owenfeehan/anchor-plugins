/*-
 * #%L
 * anchor-plugin-operator-feature
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

package org.anchoranalysis.plugin.operator.feature.bean.list;

import org.anchoranalysis.feature.bean.Feature;
import org.anchoranalysis.feature.bean.operator.FeatureListElem;
import org.anchoranalysis.feature.cache.SessionInput;
import org.anchoranalysis.feature.calc.FeatureCalculationException;
import org.anchoranalysis.feature.input.FeatureInput;
import org.apache.commons.math3.stat.descriptive.moment.GeometricMean;

// Geometric mean
public class MeanGeometric<T extends FeatureInput> extends FeatureListElem<T> {

    private GeometricMean meanCalculator = new GeometricMean();

    @Override
    public double calc(SessionInput<T> input) throws FeatureCalculationException {

        double[] result = new double[getList().size()];

        for (int i = 0; i < getList().size(); i++) {
            Feature<T> elem = getList().get(i);
            result[i] = input.calc(elem);
        }

        return meanCalculator.evaluate(result);
    }

    @Override
    public String getDscrLong() {
        StringBuilder sb = new StringBuilder();
        sb.append("geo_mean(");
        sb.append(descriptionForList(","));
        sb.append(")");
        return sb.toString();
    }
}
