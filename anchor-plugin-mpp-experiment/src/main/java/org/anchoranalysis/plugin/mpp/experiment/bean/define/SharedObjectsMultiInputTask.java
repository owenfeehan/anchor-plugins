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

package org.anchoranalysis.plugin.mpp.experiment.bean.define;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.AllowEmpty;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.error.InitException;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.core.functional.OptionalUtilities;
import org.anchoranalysis.experiment.JobExecutionException;
import org.anchoranalysis.experiment.bean.task.TaskWithoutSharedState;
import org.anchoranalysis.experiment.task.InputBound;
import org.anchoranalysis.experiment.task.InputTypesExpected;
import org.anchoranalysis.experiment.task.NoSharedState;
import org.anchoranalysis.image.bean.nonbean.init.ImageInitParams;
import org.anchoranalysis.image.io.bean.feature.OutputFeatureTable;
import org.anchoranalysis.image.stack.NamedStacksSet;
import org.anchoranalysis.io.output.bound.BoundIOContext;
import org.anchoranalysis.mpp.io.input.MultiInput;
import org.anchoranalysis.mpp.sgmn.bean.define.DefineOutputterMPP;

public class SharedObjectsMultiInputTask extends TaskWithoutSharedState<MultiInput> {

    // START BEAN PROPERTIES
    @BeanField @Getter @Setter private DefineOutputterMPP define;

    // Allows feature tables to be also outputted
    @BeanField @Getter @Setter
    private List<OutputFeatureTable> listOutputFeatureTable = new ArrayList<>();

    /** If non-empty, A keyValueParams is treated as part of the energyStack */
    @BeanField @AllowEmpty @Getter @Setter private String energyParamsName = "";
    // END BEAN PROPERTIES

    @Override
    public InputTypesExpected inputTypesExpected() {
        return new InputTypesExpected(MultiInput.class);
    }

    @Override
    public void doJobOnInputObject(InputBound<MultiInput, NoSharedState> params)
            throws JobExecutionException {

        try {
            define.processInputImage(
                    params.getInputObject(),
                    params.context(),
                    imageInitParams ->
                            outputFeatureTablesMultiplex(imageInitParams, params.context()));

        } catch (OperationFailedException e) {
            throw new JobExecutionException(e);
        }
    }

    private void outputFeatureTablesMultiplex(
            ImageInitParams imageInitParams, BoundIOContext context)
            throws OperationFailedException {

        try {
            if (define.isSuppressOutputExceptions()) {
                outputFeatureTables(imageInitParams, context);
            } else {
                outputFeatureTablesWithException(imageInitParams, context);
            }
        } catch (IOException e) {
            throw new OperationFailedException(e);
        }

        EnergyStackHelper.writeEnergyStackParams(
                imageInitParams, OptionalUtilities.create(energyParamsName), context);
    }

    @Override
    public boolean hasVeryQuickPerInputExecution() {
        return false;
    }

    private void outputFeatureTables(ImageInitParams so, BoundIOContext context) {

        for (OutputFeatureTable oft : listOutputFeatureTable) {
            try {
                oft.initRecursive(so, context.getLogger());
                oft.output(context);
            } catch (IOException | InitException e) {
                context.getErrorReporter().recordError(NamedStacksSet.class, e);
            }
        }
    }

    private void outputFeatureTablesWithException(ImageInitParams so, BoundIOContext context)
            throws IOException {
        for (OutputFeatureTable oft : listOutputFeatureTable) {

            try {
                oft.initRecursive(so, context.getLogger());
            } catch (InitException e) {
                throw new IOException(e);
            }
            oft.output(context);
        }
    }
}
