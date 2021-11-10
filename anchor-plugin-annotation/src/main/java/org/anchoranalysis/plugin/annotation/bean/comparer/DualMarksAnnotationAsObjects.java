/*-
 * #%L
 * anchor-plugin-annotation
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

package org.anchoranalysis.plugin.annotation.bean.comparer;

import java.nio.file.Path;
import java.util.Optional;
import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.annotation.io.bean.comparer.ComparableSource;
import org.anchoranalysis.annotation.io.image.findable.Findable;
import org.anchoranalysis.annotation.io.image.findable.Found;
import org.anchoranalysis.annotation.io.image.findable.NotFound;
import org.anchoranalysis.annotation.io.mark.DualMarksAnnotationReader;
import org.anchoranalysis.annotation.mark.DualMarksAnnotation;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.log.Logger;
import org.anchoranalysis.image.core.dimensions.Dimensions;
import org.anchoranalysis.image.core.object.properties.ObjectCollectionWithProperties;
import org.anchoranalysis.image.voxel.object.ObjectCollection;
import org.anchoranalysis.image.voxel.object.ObjectMask;
import org.anchoranalysis.io.input.InputReadFailedException;
import org.anchoranalysis.io.input.bean.path.DerivePath;
import org.anchoranalysis.io.input.path.DerivePathException;
import org.anchoranalysis.mpp.mark.Mark;

/**
 * Loads a {@link DualMarksAnnotation} to compared, converting each {@link Mark} into an {@link
 * ObjectMask}.
 *
 * @author Owen Feehan
 * @param <T> rejection-reason
 */
public class DualMarksAnnotationAsObjects<T> extends ComparableSource {

    // START BEAN PROPERTIES
    @BeanField @Getter @Setter private DerivePath derivePath;
    // END BEAN PROPERTIES

    @Override
    public Findable<ObjectCollection> loadAsObjects(
            Path reference, Dimensions dimensions, boolean debugMode, Logger logger)
            throws InputReadFailedException {

        Path filePath;
        try {
            filePath = derivePath.deriveFrom(reference, false);
        } catch (DerivePathException e1) {
            throw new InputReadFailedException(e1);
        }

        DualMarksAnnotationReader<T> annotationReader = new DualMarksAnnotationReader<>(false);
        Optional<DualMarksAnnotation<T>> annotation = annotationReader.read(filePath, logger);

        if (annotation.isPresent()) {
            return objectsFromAnnotation(annotation.get(), filePath, dimensions);
        } else {
            return new NotFound<>(filePath, "No annotation exists");
        }
    }

    private Findable<ObjectCollection> objectsFromAnnotation(
            DualMarksAnnotation<T> annotation, Path filePath, Dimensions dimensions) {

        if (!annotation.isAccepted()) {
            return new NotFound<>(filePath, "The annotation is NOT accepted");
        }

        ObjectCollectionWithProperties objects =
                annotation.marks().deriveObjects(dimensions, annotation.region());

        return new Found<>(objects.withoutProperties());
    }
}
