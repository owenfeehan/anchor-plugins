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

package org.anchoranalysis.plugin.image.bean.channel.provider.assign;

import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.exception.CreateException;
import org.anchoranalysis.core.identifier.provider.NamedProviderGetException;
import org.anchoranalysis.core.value.Dictionary;
import org.anchoranalysis.image.bean.provider.ChannelProviderUnary;
import org.anchoranalysis.image.core.channel.Channel;
import org.anchoranalysis.image.voxel.Voxels;
import org.anchoranalysis.image.voxel.buffer.primitive.UnsignedByteBuffer;

public class AssignFromParams extends ChannelProviderUnary {

    // START BEAN PROPERTIES
    @BeanField @Getter @Setter private String keyValueParamsID = "";

    @BeanField @Getter @Setter private String key;
    // END BEAN PROPERTIES

    @Override
    public Channel createFromChannel(Channel channel) throws CreateException {

        Dictionary params;
        try {
            params =
                    getInitializationParameters()
                            .params()
                            .getNamedKeyValueParams()
                            .getException(keyValueParamsID);
        } catch (NamedProviderGetException e) {
            throw new CreateException(
                    String.format("Cannot find KeyValueParams '%s'", keyValueParamsID), e);
        }

        if (!params.containsKey(key)) {
            throw new CreateException(String.format("Cannot find key '%s'", key));
        }

        byte valueByte = (byte) params.getAsDouble(key);

        Voxels<UnsignedByteBuffer> voxels = channel.voxels().asByte();

        int volumeXY = voxels.extent().volumeXY();
        for (int z = 0; z < voxels.extent().z(); z++) {
            UnsignedByteBuffer buffer = voxels.sliceBuffer(z);

            int offset = 0;
            while (offset < volumeXY) {
                buffer.putRaw(offset++, valueByte);
            }
        }

        return channel;
    }
}
