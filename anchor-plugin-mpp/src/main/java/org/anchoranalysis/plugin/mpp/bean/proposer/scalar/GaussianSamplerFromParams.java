/*-
 * #%L
 * anchor-plugin-mpp
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

package org.anchoranalysis.plugin.mpp.bean.proposer.scalar;

import lombok.Getter;
import lombok.Setter;
import java.util.Optional;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.shared.params.keyvalue.KeyValueParamsProvider;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.core.params.KeyValueParams;
import org.anchoranalysis.core.random.RandomNumberGenerator;
import org.anchoranalysis.image.core.dimensions.Resolution;
import org.anchoranalysis.mpp.bean.proposer.ScalarProposer;

public class GaussianSamplerFromParams extends ScalarProposer {

    // START BEAN PROPERTIES
    @BeanField @Getter @Setter private KeyValueParamsProvider params;

    @BeanField @Getter @Setter private String paramMean = "";

    @BeanField @Getter @Setter private String paramStdDev = "";

    @BeanField @Getter @Setter
    private double factorStdDev = 1.0; // Multiples the standard deviation by a factor
    // END BEAN PROPERTIES

    @Override
    public double propose(RandomNumberGenerator randomNumberGenerator, Optional<Resolution> resolution)
            throws OperationFailedException {

        try {
            KeyValueParams paramsCreated = params.create();
            assert (paramsCreated != null);

            if (!paramsCreated.containsKey(getParamMean())) {
                throw new OperationFailedException(
                        String.format("Params are missing key '%s' for paramMean", getParamMean()));
            }

            if (!paramsCreated.containsKey(getParamStdDev())) {
                throw new OperationFailedException(
                        String.format(
                                "Params are missing key '%s' for paramStdDev", getParamStdDev()));
            }

            double mean = Double.parseDouble(paramsCreated.getProperty(getParamMean()));
            double sd = Double.valueOf(paramsCreated.getProperty(getParamStdDev())) * factorStdDev;

            return randomNumberGenerator.generateNormal(mean, sd).nextDouble();
        } catch (CreateException e) {
            throw new OperationFailedException(e);
        }
    }
}
