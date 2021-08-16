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
package org.anchoranalysis.plugin.mpp.experiment.bean.objects;

import org.anchoranalysis.core.exception.OperationFailedException;
import org.anchoranalysis.image.core.object.properties.ObjectCollectionWithProperties;
import org.anchoranalysis.image.core.stack.Stack;
import org.anchoranalysis.image.io.object.output.rgb.DrawCroppedObjectsGenerator;
import org.anchoranalysis.image.io.stack.output.generator.RasterGeneratorDelegateToRaster;
import org.anchoranalysis.image.voxel.object.IntersectingObjects;
import org.anchoranalysis.image.voxel.object.ObjectMask;
import org.anchoranalysis.plugin.mpp.experiment.objects.csv.CSVRow;

class CSVRowRGBOutlineGenerator
        extends RasterGeneratorDelegateToRaster<ObjectCollectionWithProperties, CSVRow> {

    /** All objects associated with the image. */
    private final IntersectingObjects<ObjectMask> allObjects;

    public CSVRowRGBOutlineGenerator(
            DrawCroppedObjectsGenerator drawCroppedGenerator,
            IntersectingObjects<ObjectMask> allObjects) {
        super(drawCroppedGenerator);
        this.allObjects = allObjects;
    }

    @Override
    protected ObjectCollectionWithProperties convertBeforeAssign(CSVRow element)
            throws OperationFailedException {
        return element.findObjectsMatchingRow(allObjects);
    }

    @Override
    protected Stack convertBeforeTransform(Stack stack) {
        return stack;
    }
}
