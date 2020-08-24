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

package org.anchoranalysis.plugin.mpp.bean.proposer.points;

import java.awt.Color;
import java.util.List;
import java.util.Optional;
import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.anchor.mpp.bean.proposer.OrientationProposer;
import org.anchoranalysis.anchor.mpp.bean.proposer.PointsProposer;
import org.anchoranalysis.anchor.mpp.bean.proposer.ScalarProposer;
import org.anchoranalysis.anchor.mpp.mark.Mark;
import org.anchoranalysis.anchor.mpp.mark.points.PointListFactory;
import org.anchoranalysis.anchor.mpp.proposer.ProposalAbnormalFailureException;
import org.anchoranalysis.anchor.mpp.proposer.error.ErrorNode;
import org.anchoranalysis.anchor.mpp.proposer.visualization.CreateProposalVisualization;
import org.anchoranalysis.anchor.mpp.proposer.visualization.CreateProposeVisualizationList;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.annotation.OptionalBean;
import org.anchoranalysis.bean.provider.Provider;
import org.anchoranalysis.core.axis.AxisType;
import org.anchoranalysis.core.color.RGBColor;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.core.geometry.Point3d;
import org.anchoranalysis.core.geometry.Point3i;
import org.anchoranalysis.core.random.RandomNumberGenerator;
import org.anchoranalysis.image.bean.unitvalue.distance.UnitValueDistance;
import org.anchoranalysis.image.bean.unitvalue.distance.UnitValueDistanceVoxels;
import org.anchoranalysis.image.binary.mask.Mask;
import org.anchoranalysis.image.extent.Dimensions;
import org.anchoranalysis.image.extent.Resolution;
import org.anchoranalysis.image.orientation.Orientation;
import org.anchoranalysis.plugin.mpp.bean.outline.TraverseOutlineException;
import org.anchoranalysis.plugin.mpp.bean.proposer.points.fromorientation.PointsFromOrientationProposer;

public class XYOrientationExtendToZ extends PointsProposer {

    // START BEAN PROPERTIES
    @BeanField @Getter @Setter
    private OrientationProposer orientationXYProposer; // Should find an orientation in the XY plane

    @BeanField @Getter @Setter
    private PointsFromOrientationProposer pointsFromOrientationXYProposer;

    @BeanField @Getter @Setter private Provider<Mask> mask;

    @BeanField @OptionalBean @Getter @Setter private Provider<Mask> maskFilled;

    @BeanField @Getter @Setter private ScalarProposer maxDistanceZ;

    @BeanField @Getter @Setter private int minNumSlices = 3;

    @BeanField @Getter @Setter private boolean forwardDirectionOnly = false;

    // If we reach this amount of slices without adding a point, we consider our job over
    @BeanField @Getter @Setter
    private UnitValueDistance distanceZEndIfEmpty = new UnitValueDistanceVoxels(1000000);
    // END BEAN PROPERTIES

    private List<Point3i> lastPointsAll;

    @Override
    public Optional<List<Point3i>> propose(
            Point3d point,
            Mark mark,
            Dimensions dimensions,
            RandomNumberGenerator randomNumberGenerator,
            ErrorNode errorNode)
            throws ProposalAbnormalFailureException {
        pointsFromOrientationXYProposer.clearVisualizationState();

        Optional<Orientation> orientation =
                orientationXYProposer.propose(mark, dimensions, randomNumberGenerator);

        return orientation.flatMap(
                or ->
                        proposeFromOrientation(
                                or, point, dimensions, randomNumberGenerator, errorNode));
    }

    @Override
    public Optional<CreateProposalVisualization> proposalVisualization(boolean detailed) {

        CreateProposeVisualizationList list = new CreateProposeVisualizationList();

        list.add(pointsFromOrientationXYProposer.proposalVisualization(detailed));

        list.add(
                marks -> {
                    if (lastPointsAll != null && !lastPointsAll.isEmpty()) {
                        marks.addChangeID(
                                PointListFactory.createMarkFromPoints3i(lastPointsAll),
                                new RGBColor(Color.ORANGE));
                    }
                });

        return Optional.of(list);
    }

    @Override
    public boolean isCompatibleWith(Mark testMark) {
        return orientationXYProposer.isCompatibleWith(testMark);
    }

    private Optional<List<Point3i>> proposeFromOrientation(
            Orientation orientation,
            Point3d point,
            Dimensions dimensions,
            RandomNumberGenerator randomNumberGenerator,
            ErrorNode errorNode) {
        try {
            List<List<Point3i>> pointsXY =
                    getPointsFromOrientationXYProposer()
                            .calculatePoints(
                                    point,
                                    orientation,
                                    dimensions.z() > 1,
                                    randomNumberGenerator,
                                    forwardDirectionOnly);

            lastPointsAll =
                    new GeneratePointsHelper(
                                    point,
                                    channelFilled(),
                                    maxZDistance(randomNumberGenerator, dimensions.resolution()),
                                    skipZDistance(dimensions.resolution()),
                                    mask.create(),
                                    dimensions)
                            .generatePoints(pointsXY);
            return Optional.of(lastPointsAll);

        } catch (CreateException | OperationFailedException | TraverseOutlineException e1) {
            errorNode.add(e1);
            return Optional.empty();
        }
    }

    private int maxZDistance(
            RandomNumberGenerator randomNumberGenerator, Resolution resolution)
            throws OperationFailedException {
        int maxZDistance =
                (int) Math.round(maxDistanceZ.propose(randomNumberGenerator, resolution));
        maxZDistance = Math.max(maxZDistance, minNumSlices);
        return maxZDistance;
    }

    private int skipZDistance(Resolution res) throws OperationFailedException {
        return (int) Math.round(distanceZEndIfEmpty.resolveForAxis(Optional.of(res), AxisType.Z));
    }

    private Optional<Mask> channelFilled() throws CreateException {
        return maskFilled != null ? Optional.of(maskFilled.create()) : Optional.empty();
    }
}
