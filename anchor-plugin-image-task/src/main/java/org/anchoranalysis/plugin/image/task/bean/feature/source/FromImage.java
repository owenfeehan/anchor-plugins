/*-
 * #%L
 * anchor-plugin-image-task
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

package org.anchoranalysis.plugin.image.task.bean.feature.source;

import java.util.Optional;
import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.annotation.OptionalBean;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.error.InitException;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.experiment.task.InputTypesExpected;
import org.anchoranalysis.feature.bean.list.FeatureList;
import org.anchoranalysis.feature.calculate.NamedFeatureCalculateException;
import org.anchoranalysis.feature.energy.EnergyStack;
import org.anchoranalysis.feature.results.ResultsVector;
import org.anchoranalysis.image.bean.provider.stack.StackProvider;
import org.anchoranalysis.image.feature.stack.FeatureInputStack;
import org.anchoranalysis.image.io.input.ProvidesStackInput;
import org.anchoranalysis.image.stack.DisplayStack;
import org.anchoranalysis.io.output.outputter.InputOutputContext;
import org.anchoranalysis.plugin.image.bean.thumbnail.stack.ScaleToSize;
import org.anchoranalysis.plugin.image.bean.thumbnail.stack.ThumbnailFromStack;
import org.anchoranalysis.plugin.image.task.feature.InputProcessContext;
import org.anchoranalysis.plugin.image.task.feature.ResultsVectorWithThumbnail;
import org.anchoranalysis.plugin.image.task.feature.calculator.FeatureCalculatorFromProvider;

/** An image that produces one row of features. */
public class FromImage extends SingleRowPerInput<ProvidesStackInput, FeatureInputStack> {

    // START BEAN PROPERTIES
    /**
     * Optionally defines a energy-stack for feature calculation (if not set, the energy-stack is
     * considered to be the input stacks)
     */
    @BeanField @OptionalBean @Getter @Setter private StackProvider stackEnergy;

    /** Method to generate a thumbnail for images */
    @BeanField @Getter @Setter private ThumbnailFromStack thumbnail = new ScaleToSize();
    // END BEAN PROPERTIES

    public FromImage() {
        super("image");
    }

    @Override
    public InputTypesExpected inputTypesExpected() {
        return new InputTypesExpected(ProvidesStackInput.class);
    }

    @Override
    public boolean includeGroupInExperiment(boolean groupGeneratorDefined) {
        return groupGeneratorDefined;
    }

    @Override
    protected ResultsVectorWithThumbnail calculateResultsForInput(
            ProvidesStackInput input, InputProcessContext<FeatureList<FeatureInputStack>> context)
            throws NamedFeatureCalculateException {

        FeatureCalculatorFromProvider<FeatureInputStack> calculator =
                createCalculator(input, context.getContext());

        // Calculate the results for the current stack
        ResultsVector results = calculateResults(calculator, context.getRowSource());

        thumbnail.start();

        try {
            return new ResultsVectorWithThumbnail(
                    results,
                    extractThumbnail(calculator.getEnergyStack(), context.isThumbnailsEnabled()));
        } catch (CreateException e) {
            throw new NamedFeatureCalculateException(e);
        }
    }

    private ResultsVector calculateResults(
            FeatureCalculatorFromProvider<FeatureInputStack> factory,
            FeatureList<FeatureInputStack> features)
            throws NamedFeatureCalculateException {
        try {
            return factory.calculatorForAll(features).calculate(new FeatureInputStack());
        } catch (InitException e) {
            throw new NamedFeatureCalculateException(e);
        }
    }

    private Optional<DisplayStack> extractThumbnail(EnergyStack energyStack, boolean thumbnails)
            throws CreateException {
        if (thumbnails) {
            return Optional.of(thumbnail.thumbnailFor(energyStack.withoutParams().asStack()));
        } else {
            return Optional.empty();
        }
    }

    private FeatureCalculatorFromProvider<FeatureInputStack> createCalculator(
            ProvidesStackInput input, InputOutputContext context)
            throws NamedFeatureCalculateException {
        try {
            return new FeatureCalculatorFromProvider<>(
                    input, Optional.ofNullable(getStackEnergy()), context);
        } catch (OperationFailedException e) {
            throw new NamedFeatureCalculateException(e);
        }
    }
}
