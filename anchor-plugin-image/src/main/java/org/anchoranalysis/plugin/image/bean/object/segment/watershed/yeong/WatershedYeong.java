/* (C)2020 */
package org.anchoranalysis.plugin.image.bean.object.segment.watershed.yeong;

import java.nio.IntBuffer;
import java.util.Optional;
import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.OptionalFactory;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.core.geometry.Point3i;
import org.anchoranalysis.image.bean.nonbean.error.SegmentationFailedException;
import org.anchoranalysis.image.bean.segment.object.SegmentChannelIntoObjects;
import org.anchoranalysis.image.channel.Channel;
import org.anchoranalysis.image.extent.Extent;
import org.anchoranalysis.image.object.ObjectCollection;
import org.anchoranalysis.image.object.ObjectMask;
import org.anchoranalysis.image.seed.SeedCollection;
import org.anchoranalysis.image.voxel.box.VoxelBox;
import org.anchoranalysis.image.voxel.box.factory.VoxelBoxFactory;
import org.anchoranalysis.image.voxel.iterator.IterateVoxels;
import org.anchoranalysis.plugin.image.segment.watershed.encoding.EncodedVoxelBox;

/**
 * A 'rainfall' watershed algorithm
 *
 * <p><div> See:
 *
 * <ul>
 *   <li>3D watershed based on rainfall-simulation for volume segmentation, Yeong et al. 2009
 *       International Conference on Intelligent Human-Machine Systems and Cybernetics
 *   <li>An improved watershed algorithm based on efficient computation of shortest paths, Osma-Ruiz
 *       et al., Pattern Reconigion(40), 2007
 * </ul>
 *
 * </div>
 *
 * <p><div> Note:
 *
 * <ul>
 *   <li>Does not record a watershed line
 * </ul>
 *
 * </div>
 *
 * @author Owen Feehan
 */
public class WatershedYeong extends SegmentChannelIntoObjects {

    // START PROPERTIES
    /** If true, exits early and just returns the minima, without any further segmentation */
    @BeanField @Getter @Setter private boolean exitWithMinima = false;
    // END PROPERTIES

    @Override
    public ObjectCollection segment(
            Channel channel, Optional<ObjectMask> mask, Optional<SeedCollection> seeds)
            throws SegmentationFailedException {

        EncodedVoxelBox matS = createS(channel.getDimensions().getExtent());

        Optional<MinimaStore> minimaStore =
                OptionalFactory.create(exitWithMinima, MinimaStore::new);

        if (seeds.isPresent()) {
            MarkSeeds.apply(seeds.get(), matS, minimaStore, mask);
        }

        pointPixelsOrMarkAsMinima(channel.getVoxelBox().any(), matS, mask, minimaStore);

        // Special behavior where we just want to find the minima and nothing more
        if (minimaStore.isPresent()) {
            try {
                return minimaStore.get().createObjects();
            } catch (CreateException e) {
                throw new SegmentationFailedException(e);
            }
        }

        // TODO let's only work on the areas with regions
        convertAllToConnectedComponents(matS, mask);

        try {
            return createObjectsFromLabels(matS.getVoxelBox(), mask);
        } catch (CreateException e) {
            throw new SegmentationFailedException(e);
        }
    }

    /** Create 'S' matrix */
    private EncodedVoxelBox createS(Extent extent) {
        VoxelBox<IntBuffer> matSVoxelBox = VoxelBoxFactory.getInt().create(extent);
        return new EncodedVoxelBox(matSVoxelBox);
    }

    private static void pointPixelsOrMarkAsMinima(
            VoxelBox<?> vbImg,
            EncodedVoxelBox matS,
            Optional<ObjectMask> mask,
            Optional<MinimaStore> minimaStore) {

        SlidingBufferPlus buffer = new SlidingBufferPlus(vbImg, matS, mask, minimaStore);
        IterateVoxels.callEachPoint(
                mask, buffer.getSlidingBuffer(), new PointPixelsOrMarkAsMinima(buffer));
    }

    private static void convertAllToConnectedComponents(
            EncodedVoxelBox matS, Optional<ObjectMask> mask) {
        IterateVoxels.callEachPoint(
                mask, matS.getVoxelBox(), new ConvertAllToConnectedComponents(matS));
    }

    private static ObjectCollection createObjectsFromLabels(
            VoxelBox<IntBuffer> matS, Optional<ObjectMask> mask) throws CreateException {

        final BoundingBoxMap bbm = new BoundingBoxMap();

        IterateVoxels.callEachPoint(
                mask,
                matS,
                (Point3i point, IntBuffer buffer, int offset) -> {
                    int crntVal = buffer.get(offset);
                    buffer.put(offset, bbm.addPointForValue(point, crntVal) + 1);
                });

        try {
            return bbm.deriveObjects(matS);
        } catch (OperationFailedException e) {
            throw new CreateException(e);
        }
    }
}
