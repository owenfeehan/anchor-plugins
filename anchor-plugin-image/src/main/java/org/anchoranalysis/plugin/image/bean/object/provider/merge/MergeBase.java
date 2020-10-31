/*-
 * #%L
 * anchor-plugin-image
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

package org.anchoranalysis.plugin.image.bean.object.provider.merge;

import java.util.Optional;
import java.util.stream.Stream;
import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.OptionalFactory;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.annotation.OptionalBean;
import org.anchoranalysis.core.exception.CreateException;
import org.anchoranalysis.core.exception.OperationFailedException;
import org.anchoranalysis.image.bean.provider.DimensionsProvider;
import org.anchoranalysis.image.core.dimensions.Dimensions;
import org.anchoranalysis.image.core.dimensions.Resolution;
import org.anchoranalysis.image.core.dimensions.UnitConverter;
import org.anchoranalysis.image.core.object.MatchedObject;
import org.anchoranalysis.image.voxel.object.ObjectCollection;
import org.anchoranalysis.image.voxel.object.ObjectCollectionFactory;
import org.anchoranalysis.plugin.image.bean.object.match.MatcherIntersectionHelper;
import org.anchoranalysis.plugin.image.bean.object.provider.WithContainerBase;

/** A base class for algorithms that merge object-masks */
public abstract class MergeBase extends WithContainerBase {

    // START BEAN PROPERTIES
    /* Image-resolution */
    @BeanField @OptionalBean @Getter @Setter private DimensionsProvider dimensions;
    // END BEAN PROPERTIES

    @FunctionalInterface
    protected static interface MergeObjects {
        ObjectCollection mergeObjects(ObjectCollection objects) throws OperationFailedException;
    }

    protected Optional<UnitConverter> unitConvertOptional() throws OperationFailedException {
        return resolutionOptional().map(Resolution::unitConvert);
    }

    protected Resolution resolutionRequired() throws OperationFailedException {
        return resolutionOptional()
                .orElseThrow(
                        () ->
                                new OperationFailedException(
                                        "This algorithm requires an image-resolution to be set via resProvider"));
    }

    /**
     * Merges either in a container, or altogether
     *
     * @param objects
     * @param mergeFunc a function that merges a collection of objects together (changes the
     *     collection in place)
     * @return
     * @throws OperationFailedException
     */
    protected ObjectCollection mergeMultiplex(ObjectCollection objects, MergeObjects mergeFunc)
            throws OperationFailedException {

        // To avoid changing the original
        ObjectCollection objectsToMerge = objects.duplicateShallow();

        try {
            Optional<ObjectCollection> container = containerOptional();
            if (container.isPresent()) {
                return mergeInContainer(mergeFunc, objectsToMerge, container.get());
            } else {
                return mergeAll(mergeFunc, objectsToMerge);
            }
        } catch (CreateException e) {
            throw new OperationFailedException(e);
        }
    }

    private Optional<Resolution> resolutionOptional() throws OperationFailedException {
        try {
            return OptionalFactory.create(dimensions).flatMap(Dimensions::resolution);
        } catch (CreateException e) {
            throw new OperationFailedException(e);
        }
    }

    private static ObjectCollection mergeAll(MergeObjects merger, ObjectCollection objects)
            throws OperationFailedException {
        return ObjectCollectionFactory.of(merger.mergeObjects(objects));
    }

    private static ObjectCollection mergeInContainer(
            MergeObjects merger, ObjectCollection objects, ObjectCollection containerObjects)
            throws OperationFailedException {

        // All matched objects
        Stream<ObjectCollection> matchesStream =
                MatcherIntersectionHelper.matchIntersectingObjects(containerObjects, objects)
                        .stream()
                        .map(MatchedObject::getMatches);

        return ObjectCollectionFactory.flatMapFrom(
                matchesStream, OperationFailedException.class, merger::mergeObjects);
    }
}
