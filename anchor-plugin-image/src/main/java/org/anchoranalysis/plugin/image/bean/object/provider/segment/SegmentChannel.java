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

package org.anchoranalysis.plugin.image.bean.object.provider.segment;

import java.util.Optional;
import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.OptionalFactory;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.annotation.OptionalBean;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.functional.OptionalUtilities;
import org.anchoranalysis.core.geometry.Point3i;
import org.anchoranalysis.image.bean.nonbean.error.SegmentationFailedException;
import org.anchoranalysis.image.bean.provider.MaskProvider;
import org.anchoranalysis.image.bean.provider.ObjectCollectionProvider;
import org.anchoranalysis.image.bean.segment.object.SegmentChannelIntoObjects;
import org.anchoranalysis.image.binary.mask.Mask;
import org.anchoranalysis.image.channel.Channel;
import org.anchoranalysis.image.dimensions.Dimensions;
import org.anchoranalysis.image.object.ObjectCollection;
import org.anchoranalysis.image.object.ObjectMask;
import org.anchoranalysis.image.seed.SeedCollection;
import org.anchoranalysis.plugin.image.bean.object.provider.WithChannelBase;

public class SegmentChannel extends WithChannelBase {

    // START BEAN PROPERTIES
    @BeanField @OptionalBean @Getter @Setter private MaskProvider mask;

    @BeanField @Getter @Setter private SegmentChannelIntoObjects segment;

    @BeanField @OptionalBean @Getter @Setter private ObjectCollectionProvider objectsSeeds;
    // END BEAN PROPERTIES

    @Override
    protected ObjectCollection createFromChannel(Channel channelSource) throws CreateException {

        Optional<ObjectMask> maskAsObject = createObjectMask();

        try {
            return segment.segment(
                    channelSource,
                    maskAsObject,
                    createSeeds(channelSource.dimensions(), maskAsObject));
        } catch (SegmentationFailedException e) {
            throw new CreateException(e);
        }
    }

    private Optional<ObjectMask> createObjectMask() throws CreateException {
        return OptionalFactory.create(mask).map(Mask::binaryVoxels).map(ObjectMask::new);
    }

    private Optional<SeedCollection> createSeeds(
            Dimensions dimensions, Optional<ObjectMask> maskAsObject) throws CreateException {
        return OptionalUtilities.map(
                OptionalFactory.create(objectsSeeds),
                objects -> createSeeds(objects, maskAsObject, dimensions));
    }

    private static SeedCollection createSeeds(
            ObjectCollection seeds, Optional<ObjectMask> maskAsObject, Dimensions dim)
            throws CreateException {
        return OptionalUtilities.map(
                        maskAsObject,
                        object ->
                                SeedsFactory.createSeedsWithMask(
                                        seeds, object, new Point3i(0, 0, 0), dim))
                .orElseGet(() -> SeedsFactory.createSeedsWithoutMask(seeds));
    }
}
