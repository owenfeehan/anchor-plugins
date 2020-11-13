/*-
 * #%L
 * anchor-plugin-mpp-experiment
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

package org.anchoranalysis.plugin.mpp.experiment.bean.feature.report.task;

import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.anchoranalysis.core.exception.OperationFailedException;
import org.anchoranalysis.core.functional.FunctionalList;
import org.anchoranalysis.core.log.Logger;
import org.anchoranalysis.core.value.TypedValue;
import org.anchoranalysis.io.output.bean.ReportFeature;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
class ReportFeatureUtilities {

    public static <T> List<String> headerNames(
            List<? extends ReportFeature<T>> list) {
        return FunctionalList.mapToList(list, ReportFeature::title);
    }

    public static <T> List<TypedValue> elementList(
            List<? extends ReportFeature<T>> list, T featureParam, Logger logger) {

        List<TypedValue> rowElements = new ArrayList<>();

        for (ReportFeature<T> feature : list) {
            String value;
            try {
                value = feature.featureDescription(featureParam, logger);
            } catch (OperationFailedException e) {
                value = "error";
                logger.errorReporter().recordError(ReportFeatureUtilities.class, e);
            }

            rowElements.add(new TypedValue(value, feature.isNumeric()));
        }

        return rowElements;
    }
}
