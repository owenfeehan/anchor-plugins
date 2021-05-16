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

package org.anchoranalysis.plugin.mpp.bean.check;

import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.annotation.OptionalBean;
import org.anchoranalysis.bean.shared.dictionary.DictionaryProvider;
import org.anchoranalysis.core.exception.CreateException;
import org.anchoranalysis.core.exception.InitException;
import org.anchoranalysis.core.exception.OperationFailedException;
import org.anchoranalysis.core.value.Dictionary;
import org.anchoranalysis.feature.bean.Feature;
import org.anchoranalysis.feature.bean.provider.FeatureProvider;
import org.anchoranalysis.feature.calculate.FeatureCalculationException;
import org.anchoranalysis.feature.calculate.FeatureInitialization;
import org.anchoranalysis.feature.energy.EnergyStack;
import org.anchoranalysis.feature.input.FeatureInput;
import org.anchoranalysis.feature.session.FeatureSession;
import org.anchoranalysis.feature.session.calculator.single.FeatureCalculatorSingle;
import org.anchoranalysis.feature.shared.SharedFeatureMulti;
import org.anchoranalysis.mpp.bean.init.MarksInitialization;
import org.anchoranalysis.mpp.bean.regionmap.RegionMap;
import org.anchoranalysis.mpp.feature.bean.mark.CheckMark;
import org.anchoranalysis.mpp.feature.error.CheckException;
import org.anchoranalysis.mpp.mark.Mark;

public abstract class FeatureValueCheckMark<T extends FeatureInput> extends CheckMark {

    // START BEANS
    @BeanField @Getter @Setter private FeatureProvider<T> feature;

    @BeanField @Getter @Setter protected double minVal = 0;

    @BeanField @OptionalBean @Getter @Setter private DictionaryProvider dictionary;
    // END BEANS

    private SharedFeatureMulti sharedFeatureSet;

    private FeatureCalculatorSingle<T> featureCalculator;

    @Override
    public void onInit(MarksInitialization initialization) throws InitException {
        super.onInit(initialization);
        sharedFeatureSet = initialization.feature().getSharedFeatures();
    }

    @Override
    public void start(EnergyStack energyStack) throws OperationFailedException {

        try {
            Feature<T> featureCreated = feature.create();

            Dictionary dictionaryCreated = createDictionary();

            featureCalculator =
                    FeatureSession.with(
                            featureCreated,
                            new FeatureInitialization(dictionaryCreated),
                            sharedFeatureSet,
                            getLogger());

        } catch (CreateException | InitException e) {
            featureCalculator = null;
            throw new OperationFailedException(e);
        }
    }

    @Override
    public boolean check(Mark mark, RegionMap regionMap, EnergyStack energyStack)
            throws CheckException {

        if (featureCalculator == null) {
            throw new CheckException("No session initialized");
        }

        try {
            double energy =
                    featureCalculator.calculate(
                            createFeatureCalcParams(mark, regionMap, energyStack));

            return (energy >= minVal);

        } catch (FeatureCalculationException e) {

            throw new CheckException(String.format("Error calculating feature:%n%s", e));
        }
    }

    protected abstract T createFeatureCalcParams(
            Mark mark, RegionMap regionMap, EnergyStack energyStack);

    private Dictionary createDictionary() throws CreateException {
        if (dictionary != null) {
            return dictionary.create();
        } else {
            return new Dictionary();
        }
    }

    @Override
    public boolean isCompatibleWith(Mark testMark) {
        return true;
    }
}
