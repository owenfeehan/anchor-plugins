/* (C)2020 */
package org.anchoranalysis.plugin.operator.feature.bean.replace;

import org.anchoranalysis.feature.input.FeatureInput;
import org.anchoranalysis.plugin.operator.feature.bean.FeatureGenericWithValue;

/**
 * Calculates the underlying feature, but replaces the result with a constant if it happens to be an
 * "unusual" value
 *
 * @author Owen Feehan
 * @param <T>
 */
public abstract class ReplaceUnusualValue<T extends FeatureInput>
        extends FeatureGenericWithValue<T> {

    @Override
    protected double combineValueAndFeature(double value, double featureResult) {
        if (!isResultUnusual(featureResult)) {
            return featureResult;
        } else {
            return value;
        }
    }

    @Override
    protected String combineDscr(String valueDscr, String featureDscr) {
        return featureDscr;
    }

    protected abstract boolean isResultUnusual(double featureResult);
}
