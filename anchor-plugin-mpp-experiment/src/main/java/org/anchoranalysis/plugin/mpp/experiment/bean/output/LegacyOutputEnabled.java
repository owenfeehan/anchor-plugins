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

package org.anchoranalysis.plugin.mpp.experiment.bean.output;

import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.io.output.bean.enabled.All;
import org.anchoranalysis.io.output.bean.enabled.None;
import org.anchoranalysis.io.output.bean.enabled.OutputEnabled;
import org.anchoranalysis.io.output.bean.rules.OutputEnabledRules;
import org.anchoranalysis.mpp.io.output.StackOutputKeys;

/**
 * Legacy rules for whether outputs are enabled or not.
 *
 * @author Owen Feehan
 * @deprecated Prefer another sub-class of {@link OutputEnabledRules}.
 */
public class LegacyOutputEnabled extends OutputEnabledRules {

    // BEAN PROPERTIES
    /** What's allowed or not - highest level outputs */
    @BeanField @Getter @Setter private OutputEnabled outputEnabled = All.INSTANCE;

    /** What's allowed or not when outputting stacks */
    @BeanField @Getter @Setter private OutputEnabled stacksOutputEnabled = All.INSTANCE;

    /** What's allowed or not when outputting configurations */
    @BeanField @Getter @Setter private OutputEnabled marksOutputEnabled = All.INSTANCE;

    /** What's allowed or not when outputting object-collections */
    @BeanField @Getter @Setter private OutputEnabled objects = All.INSTANCE;

    /** What's allowed or not when outputting histograms */
    @BeanField @Getter @Setter private OutputEnabled histogramsOutputEnabled = All.INSTANCE;
    // END BEAN PROPERTIES

    @Override
    public OutputEnabled first() {
        // TODO Auto-generated method stub
        return outputEnabled;
    }

    @Override
    public OutputEnabled second(String outputName) {
        switch (outputName) {
            case StackOutputKeys.STACK:
                return getStacksOutputEnabled();
            case StackOutputKeys.MARKS:
                return getMarksOutputEnabled();
            case StackOutputKeys.HISTOGRAM:
                return getHistogramsOutputEnabled();
            case StackOutputKeys.OBJECTS:
                return getObjects();
            default:
                return new None();
        }
    }
}
