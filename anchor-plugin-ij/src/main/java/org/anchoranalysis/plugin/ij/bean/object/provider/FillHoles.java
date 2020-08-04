/*-
 * #%L
 * anchor-plugin-ij
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

package org.anchoranalysis.plugin.ij.bean.object.provider;

import ch.ethz.biol.cell.imageprocessing.binaryimgchnl.provider.BinaryChnlProviderIJBinary;
import java.nio.ByteBuffer;
import java.util.Optional;
import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.OptionalFactory;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.annotation.OptionalBean;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.image.bean.provider.MaskProvider;
import org.anchoranalysis.image.bean.provider.ObjectCollectionProviderUnary;
import org.anchoranalysis.image.binary.mask.Mask;
import org.anchoranalysis.image.binary.voxel.BinaryVoxels;
import org.anchoranalysis.image.extent.BoundingBox;
import org.anchoranalysis.image.object.ObjectCollection;
import org.anchoranalysis.image.object.ObjectMask;

/**
 * Fills holes in an object. Existing object-masks are overwritten (i.e. their memory buffers are
 * replaced with filled-in pixels).
 *
 * <p>An optional mask which restricts where a fill operation can happen TODO make this an immutable
 * provider that always returns a new object-collection.
 *
 * @author Owen Feehan
 */
public class FillHoles extends ObjectCollectionProviderUnary {

    // START BEAN PROPERTIES
    /** */
    @BeanField @OptionalBean @Getter @Setter private MaskProvider mask;
    // END BEAN PROPERTIES

    @Override
    public ObjectCollection createFromObjects(ObjectCollection objects) throws CreateException {

        Optional<Mask> maskChnl = OptionalFactory.create(mask);

        for (ObjectMask objectMask : objects) {

            BinaryVoxels<ByteBuffer> bvb = objectMask.binaryVoxels();
            BinaryVoxels<ByteBuffer> bvbDup = bvb.duplicate();

            try {
                BinaryChnlProviderIJBinary.fill(bvbDup);
            } catch (OperationFailedException e) {
                throw new CreateException(e);
            }

            if (maskChnl.isPresent()) {
                // Let's make an object for our mask
                ObjectMask objectRegion = maskChnl.get().region(objectMask.getBoundingBox(), true);

                BoundingBox bboxAll = new BoundingBox(bvb.extent());

                // We do an and operation with the mask
                bvbDup.copyPixelsToCheckMask(
                        bboxAll,
                        bvb.getVoxels(),
                        bboxAll,
                        objectRegion.getVoxels(),
                        objectRegion.getBinaryValuesByte());
            }
        }
        return objects;
    }
}
