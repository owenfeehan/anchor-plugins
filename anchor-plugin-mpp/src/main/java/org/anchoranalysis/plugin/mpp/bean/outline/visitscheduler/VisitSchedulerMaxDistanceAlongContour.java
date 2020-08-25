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

package org.anchoranalysis.plugin.mpp.bean.outline.visitscheduler;

import java.util.Optional;
import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.error.InitException;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.core.geometry.Point3i;
import org.anchoranalysis.core.geometry.Tuple3i;
import org.anchoranalysis.core.random.RandomNumberGenerator;
import org.anchoranalysis.image.extent.Resolution;
import org.anchoranalysis.image.object.ObjectMask;
import org.anchoranalysis.mpp.bean.proposer.ScalarProposer;

// Breadth-first iteration of pixels
public class VisitSchedulerMaxDistanceAlongContour extends VisitScheduler {

    // START BEAN PROPERTIES
    @BeanField @Getter @Setter private ScalarProposer maxDistanceProposer;
    // END BEAN PROPERTIES

    private double maxDistance;

    @Override
    public void beforeCreateObject(RandomNumberGenerator randomNumberGenerator, Resolution res)
            throws InitException {
        try {
            maxDistance = maxDistanceProposer.propose(randomNumberGenerator, res);

            assert (maxDistance > 0);
        } catch (OperationFailedException e) {
            throw new InitException(e);
        }
    }

    @Override
    public Optional<Tuple3i> maxDistanceFromRootPoint(Resolution res) {
        int maxDistanceInt = (int) Math.ceil(this.maxDistance);
        assert (maxDistanceInt > 0);
        return Optional.of(new Point3i(maxDistanceInt, maxDistanceInt, maxDistanceInt));
    }

    @Override
    public void afterCreateObject(
            Point3i root, Resolution res, RandomNumberGenerator randomNumberGenerator)
            throws InitException {
        // NOTHING TO DO
    }

    @Override
    public boolean considerVisit(Point3i point, int distanceAlongContour, ObjectMask object) {
        return (distanceAlongContour <= maxDistance);
    }
}
