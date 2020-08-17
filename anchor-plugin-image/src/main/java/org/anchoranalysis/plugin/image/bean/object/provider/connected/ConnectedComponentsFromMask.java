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

package org.anchoranalysis.plugin.image.bean.object.provider.connected;

import java.nio.ByteBuffer;
import java.util.Optional;
import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.image.bean.nonbean.error.UnitValueException;
import org.anchoranalysis.image.bean.provider.MaskProvider;
import org.anchoranalysis.image.bean.provider.ObjectCollectionProvider;
import org.anchoranalysis.image.bean.unitvalue.areavolume.UnitValueAreaOrVolume;
import org.anchoranalysis.image.bean.unitvalue.volume.UnitValueVolumeVoxels;
import org.anchoranalysis.image.binary.mask.Mask;
import org.anchoranalysis.image.binary.values.BinaryValues;
import org.anchoranalysis.image.binary.voxel.BinaryVoxels;
import org.anchoranalysis.image.binary.voxel.BinaryVoxelsFactory;
import org.anchoranalysis.image.object.ObjectCollection;
import org.anchoranalysis.image.object.ObjectCollectionFactory;
import org.anchoranalysis.image.object.factory.CreateFromConnectedComponentsFactory;
import org.anchoranalysis.image.voxel.extracter.VoxelsExtracter;
import org.apache.commons.lang.time.StopWatch;

/**
 * Converts a binary-mask into its connected components
 *
 * @author feehano
 */
public class ConnectedComponentsFromMask extends ObjectCollectionProvider {

    // START BEAN PROPERTIES
    @BeanField @Getter @Setter private MaskProvider binaryChnl;

    @BeanField @Getter @Setter
    private UnitValueAreaOrVolume minVolume = new UnitValueVolumeVoxels(1);

    @BeanField @Getter @Setter private boolean bySlices = false;

    /** If true uses 8 neighborhood rather than 4 neighborhood etc. in 2D, and similar in 3D */
    @BeanField @Getter @Setter private boolean bigNeighborhood = false;
    // END BEAN PROPERTIES

    @Override
    public ObjectCollection create() throws CreateException {

        Mask mask = binaryChnl.create();

        StopWatch sw = new StopWatch();
        sw.start();

        try {
            int minNumberVoxels =
                    (int)
                            Math.round(
                                    minVolume.resolveToVoxels(
                                            Optional.of(mask.dimensions().resolution())));

            if (bySlices) {
                return createObjectsBySlice(mask, minNumberVoxels);
            } else {
                return createObjects3D(mask, minNumberVoxels);
            }

        } catch (UnitValueException e) {
            throw new CreateException(e);
        }
    }

    private CreateFromConnectedComponentsFactory createFactory(int minNumberVoxels) {
        return new CreateFromConnectedComponentsFactory(bigNeighborhood, minNumberVoxels);
    }

    private ObjectCollection createObjects3D(Mask mask, int minNumberVoxels) throws CreateException {
        return createFactory(minNumberVoxels).createConnectedComponents(mask);
    }

    private ObjectCollection createObjectsBySlice(Mask mask, int minNumberVoxels)
            throws CreateException {

        CreateFromConnectedComponentsFactory creator = createFactory(minNumberVoxels);

        VoxelsExtracter<ByteBuffer> extracter = mask.voxels().extracter();
        
        return ObjectCollectionFactory.flatMapFromRange(
                0,
                mask.dimensions().z(),
                CreateException.class,
                z -> createForSlice(creator, extractSlice(extracter, z, mask.binaryValues()), z));
    }

    private static BinaryVoxels<ByteBuffer> extractSlice(VoxelsExtracter<ByteBuffer> extracter, int z, BinaryValues binaryValues) {
        return BinaryVoxelsFactory.reuseByte(extracter.slice(z), binaryValues);
    }

    private ObjectCollection createForSlice(
            CreateFromConnectedComponentsFactory objectCreator,
            BinaryVoxels<ByteBuffer> bvb,
            int z) {
        // respecify the z
        return objectCreator.createConnectedComponents(bvb).stream()
                .mapBoundingBoxPreserveExtent(box -> box.shiftToZ(z));
    }
}
