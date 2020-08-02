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

package ch.ethz.biol.cell.imageprocessing.binaryimgchnl.provider;

import ch.ethz.biol.cell.imageprocessing.chnl.provider.DimChecker;
import java.util.Optional;
import org.anchoranalysis.bean.OptionalFactory;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.image.bean.provider.ChannelProvider;
import org.anchoranalysis.image.bean.provider.MaskProvider;
import org.anchoranalysis.image.binary.mask.Mask;
import org.anchoranalysis.image.channel.Channel;
import org.anchoranalysis.image.extent.ImageDimensions;

/**
 * Like {@link OptionalFactory} but with specific methods for {@link Channel} and {@link Mask}
 * including checking channel sizes.
 *
 * @author Owen Feehan
 */
class ChnlProviderNullableCreator {

    private ChnlProviderNullableCreator() {}

    public static Optional<Channel> createOptionalCheckSize(
            ChannelProvider chnlProvider, String chnlProviderName, ImageDimensions dim)
            throws CreateException {
        Optional<Channel> chnl = OptionalFactory.create(chnlProvider);
        if (chnl.isPresent()) {
            DimChecker.check(chnl.get(), chnlProviderName, dim);
        }
        return chnl;
    }

    public static Optional<Mask> createOptionalCheckSize(
            MaskProvider chnlProvider, String chnlProviderName, ImageDimensions dim)
            throws CreateException {
        Optional<Mask> chnl = OptionalFactory.create(chnlProvider);
        if (chnl.isPresent()) {
            DimChecker.check(chnl.get(), chnlProviderName, dim);
        }
        return chnl;
    }
}
