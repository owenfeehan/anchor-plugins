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

package ch.ethz.biol.cell.imageprocessing.chnl.provider;

import ch.ethz.biol.cell.mpp.nrg.feature.pixelwise.createvoxelbox.CreateVoxelsFromPixelwiseFeature;
import ch.ethz.biol.cell.mpp.nrg.feature.pixelwise.createvoxelbox.CreateVoxelsFromPixelwiseFeatureWithMask;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.annotation.OptionalBean;
import org.anchoranalysis.bean.shared.params.keyvalue.KeyValueParamsProvider;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.params.KeyValueParams;
import org.anchoranalysis.image.bean.provider.ChannelProvider;
import org.anchoranalysis.image.bean.provider.HistogramProvider;
import org.anchoranalysis.image.bean.provider.MaskProvider;
import org.anchoranalysis.image.binary.mask.Mask;
import org.anchoranalysis.image.channel.Channel;
import org.anchoranalysis.image.channel.factory.ChannelFactoryByte;
import org.anchoranalysis.image.extent.BoundingBox;
import org.anchoranalysis.image.feature.bean.pixelwise.PixelScore;
import org.anchoranalysis.image.histogram.Histogram;
import org.anchoranalysis.image.object.ObjectMask;
import org.anchoranalysis.image.voxel.Voxels;
import org.anchoranalysis.image.voxel.VoxelsWrapper;
import org.anchoranalysis.image.voxel.VoxelsWrapperList;

public class ChnlProviderPixelScore extends ChannelProvider {

    // START BEAN PROPERTIES
    @BeanField @Getter @Setter private ChannelProvider intensityProvider;

    @BeanField @OptionalBean @Getter @Setter private ChannelProvider gradientProvider;

    // We don't use {@link ChnlProiderMask} as here it's optional.
    @BeanField @OptionalBean @Getter @Setter private MaskProvider mask;

    @BeanField @Getter @Setter private PixelScore pixelScore;

    @BeanField @Getter @Setter
    private List<ChannelProvider> listChnlProviderExtra = new ArrayList<>();

    @BeanField @Getter @Setter
    private List<HistogramProvider> listHistogramProviderExtra = new ArrayList<>();

    @BeanField @OptionalBean @Getter @Setter private KeyValueParamsProvider keyValueParamsProvider;
    // END BEAN PROPERTIES

    private VoxelsWrapperList createVoxelsList(Channel chnlIntensity) throws CreateException {

        VoxelsWrapperList listOut = new VoxelsWrapperList();

        listOut.add(chnlIntensity.voxels());

        if (gradientProvider != null) {
            listOut.add(gradientProvider.create().voxels());
        }
        for (ChannelProvider chnlProvider : listChnlProviderExtra) {
            VoxelsWrapper voxelsExtra =
                    chnlProvider != null ? chnlProvider.create().voxels() : null;
            listOut.add(voxelsExtra);
        }
        return listOut;
    }

    private Optional<ObjectMask> createMaskOrNull() throws CreateException {
        if (mask == null) {
            return Optional.empty();
        }

        Mask createdMask = mask.create();
        Channel chnlMask = createdMask.channel();

        return Optional.of(
                new ObjectMask(
                        new BoundingBox(chnlMask.dimensions().extent()),
                        chnlMask.voxels().asByte(),
                        createdMask.binaryValues()));
    }

    @Override
    public Channel create() throws CreateException {

        Channel chnlIntensity = intensityProvider.create();

        VoxelsWrapperList listVb = createVoxelsList(chnlIntensity);
        List<Histogram> listHistExtra =
                ProviderBeanUtilities.listFromBeans(listHistogramProviderExtra);

        Optional<KeyValueParams> kpv;
        if (keyValueParamsProvider != null) {
            kpv = Optional.of(keyValueParamsProvider.create());
        } else {
            kpv = Optional.empty();
        }

        Optional<ObjectMask> object = createMaskOrNull();

        Voxels<ByteBuffer> voxelsPixelScore;
        if (object.isPresent()) {
            CreateVoxelsFromPixelwiseFeatureWithMask creator =
                    new CreateVoxelsFromPixelwiseFeatureWithMask(listVb, kpv, listHistExtra);

            voxelsPixelScore = creator.createVoxelsFromPixelScore(pixelScore, object);

        } else {
            CreateVoxelsFromPixelwiseFeature creator =
                    new CreateVoxelsFromPixelwiseFeature(listVb, kpv, listHistExtra);

            voxelsPixelScore = creator.createVoxelsFromPixelScore(pixelScore);
        }

        return new ChannelFactoryByte()
                .create(voxelsPixelScore, chnlIntensity.dimensions().resolution());
    }
}
