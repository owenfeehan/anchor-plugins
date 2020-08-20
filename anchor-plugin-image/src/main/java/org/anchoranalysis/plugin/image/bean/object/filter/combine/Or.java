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

package org.anchoranalysis.plugin.image.bean.object.filter.combine;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.image.bean.object.ObjectFilter;
import org.anchoranalysis.image.extent.Dimensions;
import org.anchoranalysis.image.object.ObjectCollection;
import org.anchoranalysis.image.object.ObjectCollectionFactory;
import org.anchoranalysis.image.object.ObjectMask;

/**
 * Applies multiples filter with logical OR i.e. an object must pass any one of the filter steps to
 * remain.
 *
 * @author Owen Feehan
 */
public class Or extends ObjectFilterCombine {

    @Override
    public ObjectCollection filter(
            ObjectCollection objectsToFilter,
            Optional<Dimensions> dim,
            Optional<ObjectCollection> objectsRejected)
            throws OperationFailedException {

        // Stores any successful items in a set
        Set<ObjectMask> setAccepted = findAcceptedObjects(objectsToFilter, dim);

        // Adds the rejected-objects
        objectsRejected.ifPresent(
                rejected -> rejected.addAll(determineRejected(objectsToFilter, setAccepted)));

        // Creates the accepted-objects
        return ObjectCollectionFactory.fromSet(setAccepted);
    }

    /** Finds the accepted objects (i.e. objects that pass any one of the filters) */
    private Set<ObjectMask> findAcceptedObjects(
            ObjectCollection objectsToFilter, Optional<Dimensions> dim)
            throws OperationFailedException {
        Set<ObjectMask> setAccepted = new HashSet<>();

        for (ObjectFilter indFilter : getList()) {
            setAccepted.addAll(indFilter.filter(objectsToFilter, dim, Optional.empty()).asList());
        }

        return setAccepted;
    }

    /** Determines which objects are rejected */
    private static ObjectCollection determineRejected(
            ObjectCollection objectsToFilter, Set<ObjectMask> setAccepted) {
        return objectsToFilter.stream().filter(object -> !setAccepted.contains(object));
    }
}
