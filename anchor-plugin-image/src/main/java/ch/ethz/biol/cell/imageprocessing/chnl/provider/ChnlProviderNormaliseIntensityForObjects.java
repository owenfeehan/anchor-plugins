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
/* (C)2020 */
package ch.ethz.biol.cell.imageprocessing.chnl.provider;

import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.image.channel.Channel;
import org.anchoranalysis.image.object.ObjectCollection;
import org.anchoranalysis.image.object.ObjectMask;
import org.anchoranalysis.image.voxel.box.VoxelBox;
import org.anchoranalysis.plugin.image.intensity.IntensityMeanCalculator;

// Rewrites the intensity for each object-mask (assume no overlap) so that its mean is 128
public class ChnlProviderNormaliseIntensityForObjects extends ChnlProviderOneObjectsSource {

    @Override
    protected Channel createFromChnl(Channel chnl, ObjectCollection objectsSource)
            throws CreateException {

        VoxelBox<?> vb = chnl.getVoxelBox().any();

        for (ObjectMask object : objectsSource) {

            try {
                double meanIntensity =
                        IntensityMeanCalculator.calcMeanIntensityObject(chnl, object);

                if (meanIntensity == 0.0) {
                    // Special case. The mean can only be 0.0, if all pixels are 0
                    vb.setPixelsCheckMask(object, 128);
                    continue;
                }

                double scaleFactor = 128 / meanIntensity;
                vb.scalePixelsCheckMask(object, scaleFactor);
            } catch (FeatureCalcException e) {
                throw new CreateException(e);
            }
        }

        return chnl;
    }
}
