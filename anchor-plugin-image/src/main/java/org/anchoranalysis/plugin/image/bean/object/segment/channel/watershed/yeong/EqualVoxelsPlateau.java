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

package org.anchoranalysis.plugin.image.bean.object.segment.channel.watershed.yeong;

import com.google.common.base.Preconditions;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import lombok.Getter;
import org.anchoranalysis.core.functional.FunctionalList;
import org.anchoranalysis.spatial.point.Point3i;

class EqualVoxelsPlateau {

    @Getter private List<PointWithNeighbor> pointsEdge = new LinkedList<>();

    // TODO this runs really slowly if this is switched to a LinkedList. Find out why.
    @Getter private List<Point3i> pointsInner = new ArrayList<>();

    public void addEdge(Point3i point, int neighborIndex) {
        Preconditions.checkArgument(neighborIndex >= 0);
        pointsEdge.add(new PointWithNeighbor(point, neighborIndex));
    }

    public void addInner(Point3i point) {
        // We make we duplicate, as point is coming from an iterator and is mutable
        pointsInner.add(new Point3i(point));
    }

    public boolean hasPoints() {
        return !pointsEdge.isEmpty() || !pointsInner.isEmpty();
    }

    public boolean isOnlyEdge() {
        return pointsInner.isEmpty() && !pointsEdge.isEmpty();
    }

    public boolean isOnlyInner() {
        return pointsEdge.isEmpty() && !pointsInner.isEmpty();
    }

    // TODO EFFICIENCY, rewrite as two separate lists, avoids having to make new lists
    public List<Point3i> pointsEdge() {
        return FunctionalList.mapToList(pointsEdge, PointWithNeighbor::getPoint);
    }

    public boolean hasNullItems() {

        for (Point3i point : pointsInner) {
            if (point == null) {
                return true;
            }
        }

        for (PointWithNeighbor pointWithNeighbor : pointsEdge) {
            if (pointWithNeighbor == null) {
                return true;
            }
        }
        return false;
    }

    public int size() {
        return pointsEdge.size() + pointsInner.size();
    }
}
