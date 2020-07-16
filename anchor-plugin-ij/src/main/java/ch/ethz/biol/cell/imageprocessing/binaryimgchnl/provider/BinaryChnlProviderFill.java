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

package ch.ethz.biol.cell.imageprocessing.binaryimgchnl.provider;

import java.util.Optional;
import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.annotation.OptionalBean;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.image.bean.nonbean.error.UnitValueException;
import org.anchoranalysis.image.bean.provider.BinaryChnlProviderOne;
import org.anchoranalysis.image.bean.unitvalue.areavolume.UnitValueAreaOrVolume;
import org.anchoranalysis.image.binary.logical.BinaryChnlAnd;
import org.anchoranalysis.image.binary.logical.BinaryChnlOr;
import org.anchoranalysis.image.binary.mask.Mask;
import org.anchoranalysis.image.binary.values.BinaryValues;
import org.anchoranalysis.image.extent.ImageDimensions;
import org.anchoranalysis.image.object.ObjectCollection;
import org.anchoranalysis.image.object.ObjectMask;
import org.anchoranalysis.image.object.factory.CreateFromConnectedComponentsFactory;
import org.anchoranalysis.image.object.ops.BinaryChnlFromObjects;

public class BinaryChnlProviderFill extends BinaryChnlProviderOne {

    // START BEAN PROPERTIES
    @BeanField @OptionalBean @Getter @Setter private UnitValueAreaOrVolume maxVolume = null;

    /** If true we do not fill any objects that touch the border */
    @BeanField @Getter @Setter private boolean skipAtBorder = true;
    // END BEAN PROPERTIES

    @Override
    public Mask createFromChnl(Mask bic) throws CreateException {

        Mask bicDup = fillChnl(bic);

        return fillHoles(
                filterObjectsFromBinaryImage(bicDup),
                bic,
                bic.getDimensions(),
                BinaryValues.getDefault());
    }

    private Mask fillChnl(Mask bic) throws CreateException {
        Mask bicInv = new Mask(bic.getChannel(), bic.getBinaryValues().createInverted());

        Mask bicDup = bic.duplicate();

        try {
            BinaryChnlProviderIJBinary.fill(bicDup.binaryVoxelBox());
        } catch (OperationFailedException e1) {
            throw new CreateException(e1);
        }

        BinaryChnlAnd.apply(bicDup, bicInv);

        return bicDup;
    }

    private ObjectCollection filterObjectsFromBinaryImage(Mask bi) throws CreateException {

        CreateFromConnectedComponentsFactory objectCreator =
                new CreateFromConnectedComponentsFactory();

        return filterObjects(objectCreator.createConnectedComponents(bi), bi.getDimensions());
    }

    private ObjectCollection filterObjects(ObjectCollection objects, ImageDimensions dimensions)
            throws CreateException {

        final double maxVolumeResolved = determineMaxVolume(dimensions);

        return objects.stream()
                .filter(objectMask -> includeObject(objectMask, dimensions, maxVolumeResolved));
    }

    private double determineMaxVolume(ImageDimensions dim) throws CreateException {
        if (maxVolume != null) {
            try {
                return maxVolume.resolveToVoxels(Optional.of(dim.getRes()));
            } catch (UnitValueException e) {
                throw new CreateException(e);
            }
        } else {
            return 0; // Arbitrary, as it will be ignored anyway
        }
    }

    private boolean includeObject(
            ObjectMask object, ImageDimensions dimensions, double maxVolumeResolved) {
        // It's not allowed touch the border
        if (skipAtBorder && object.getBoundingBox().atBorderXY(dimensions)) {
            return false;
        }

        // Volume check
        return maxVolume == null || object.numberVoxelsOn() <= maxVolumeResolved;
    }

    private static Mask fillHoles(
            ObjectCollection filled, Mask src, ImageDimensions sd, BinaryValues bvOut) {
        Mask bcSelected = BinaryChnlFromObjects.createFromObjects(filled, sd, bvOut);
        BinaryChnlOr.binaryOr(bcSelected, src);
        return bcSelected;
    }
}
