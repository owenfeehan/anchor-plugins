/* (C)2020 */
package org.anchoranalysis.plugin.image.bean.object.segment.watershed.yeong;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.geometry.Point3i;
import org.anchoranalysis.image.binary.values.BinaryValuesByte;
import org.anchoranalysis.image.object.ObjectMask;
import org.anchoranalysis.image.voxel.iterator.IterateVoxels;
import org.anchoranalysis.image.voxel.iterator.changed.ProcessChangedPointAbsoluteMasked;
import org.anchoranalysis.image.voxel.iterator.changed.ProcessVoxelNeighbor;
import org.anchoranalysis.image.voxel.iterator.changed.ProcessVoxelNeighborFactory;
import org.anchoranalysis.image.voxel.neighborhood.Neighborhood;
import org.anchoranalysis.image.voxel.neighborhood.NeighborhoodFactory;
import org.anchoranalysis.plugin.image.segment.watershed.encoding.EncodedVoxelBox;

class MakePlateauLowerComplete {

    private static class PointTester implements ProcessChangedPointAbsoluteMasked<List<Point3i>> {

        // STATIC
        private EncodedVoxelBox matS;

        private final List<Point3i> foundPoints = new ArrayList<>();

        private int zChange;
        private ByteBuffer bb;
        private int z1;
        private final byte maskValueOff;

        public PointTester(EncodedVoxelBox matS, BinaryValuesByte bv) {
            super();
            this.matS = matS;
            this.maskValueOff = bv.getOffByte();
        }

        @Override
        public void initSource(int sourceVal, int sourceOffsetXY) {
            foundPoints.clear();
        }

        @Override
        public void notifyChangeZ(int zChange, int z1, ByteBuffer objectMaskBuffer) {
            this.bb = objectMaskBuffer;
            this.zChange = zChange;
            this.z1 = z1;
        }

        @Override
        public boolean processPoint(
                int xChange, int yChange, int x1, int y1, int objectMaskOffset) {

            Point3i pointRel = new Point3i(x1, y1, z1);
            foundPoints.add(pointRel);
            bb.put(objectMaskOffset, maskValueOff);

            // We point this value in the direction opposite to which it came
            matS.setPointDirection(pointRel, xChange * -1, yChange * -1, zChange * -1);

            return true;
        }

        @Override
        public List<Point3i> collectResult() {
            return foundPoints;
        }
    }

    private EqualVoxelsPlateau plateau;
    private boolean do3D;

    public MakePlateauLowerComplete(EqualVoxelsPlateau plateau, boolean do3D) {
        super();
        this.plateau = plateau;
        this.do3D = do3D;
    }

    public void makeBufferLowerCompleteForPlateau(
            EncodedVoxelBox matS, Optional<MinimaStore> minimaStore) {

        assert (plateau.hasPoints());
        if (plateau.isOnlyEdge()) {
            pointEdgeToNeighboring(matS);
        } else if (plateau.isOnlyInner()) {
            // EVERYTHING COLLECTIVELY IS A LOCAL MINIMA
            // We pick one pixel to use as an index, and pointing the rest of the pixels
            //  of them towards it

            assert plateau.getPointsInner().size() >= 2;

            matS.pointListAtFirstPoint(plateau.getPointsInner());

            if (minimaStore.isPresent()) {
                minimaStore.get().add(plateau.getPointsInner());
            }
        } else {

            // IF IT'S MIXED...

            pointEdgeToNeighboring(matS);
            pointInnerToEdge(matS);
        }
    }

    private void pointEdgeToNeighboring(EncodedVoxelBox matS) {
        // We set them all to their neighboring points
        for (PointWithNeighbor pointNeighbor : plateau.getPointsEdge()) {
            matS.setPoint(pointNeighbor.getPoint(), pointNeighbor.getNeighborIndex());
        }
    }

    private void pointInnerToEdge(EncodedVoxelBox matS) {
        // Iterate through each edge pixel, and look for neighboring points in the Inner pixels
        //   for any such point, point towards the edge pixel, and move to the new edge list
        List<Point3i> searchPoints = plateau.pointsEdge();

        try {
            // We create an object-mask from the list of points
            ObjectMask object = CreateObjectFromPoints.create(plateau.getPointsInner());
            Neighborhood neighborhood = NeighborhoodFactory.of(true);

            ProcessVoxelNeighbor<List<Point3i>> process =
                    ProcessVoxelNeighborFactory.withinMask(
                            object, new PointTester(matS, object.getBinaryValuesByte()));

            while (!searchPoints.isEmpty()) {
                searchPoints = findPointsFor(searchPoints, neighborhood, process);
            }

        } catch (CreateException e) {
            // the only exception possible should be when there are 0 pixels
            assert false;
        }
    }

    private List<Point3i> findPointsFor(
            List<Point3i> points,
            Neighborhood neighborhood,
            ProcessVoxelNeighbor<List<Point3i>> process) {

        List<Point3i> foundPoints = new ArrayList<>();

        // We iterate through all the search points
        for (Point3i point : points) {

            foundPoints.addAll(
                    IterateVoxels.callEachPointInNeighborhood(
                            point,
                            neighborhood,
                            do3D,
                            process,
                            -1, // The -1 value are arbitrary, as it will be ignored
                            -1 // The -1 value are arbitrary, as it will be ignored
                            ));
        }
        return foundPoints;
    }
}
