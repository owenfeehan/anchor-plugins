/*-
 * #%L
 * anchor-plugin-image-task
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

package org.anchoranalysis.plugin.image.task.bean.format.convertsyle;

import java.util.Set;
import java.util.function.BiConsumer;
import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.annotation.OptionalBean;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.core.index.GetOperationFailedException;
import org.anchoranalysis.core.log.Logger;
import org.anchoranalysis.core.log.MessageLogger;
import org.anchoranalysis.image.extent.IncorrectImageSizeException;
import org.anchoranalysis.image.stack.Stack;
import org.anchoranalysis.io.error.AnchorIOException;
import org.anchoranalysis.plugin.image.task.channel.ChannelGetterForTimepoint;

public class RGB extends ChannelConvertStyle {

    // START BEAN PROPERTIES
    /**
     * If a channel doesn't match an RGB pattern, this conversion-style can be used instead.
     *
     * <p>If unset, an error is instead thrown in this circumstances
     */
    @BeanField @OptionalBean @Getter @Setter private ChannelConvertStyle fallback;
    // END BEAN PROPERTIES

    @Override
    public void convert(
            Set<String> channelNames,
            ChannelGetterForTimepoint channelGetter,
            BiConsumer<String, Stack> stacksOut,
            Logger logger)
            throws AnchorIOException {

        if (!channelNamesAreRGB(channelNames)) {
            // Not compatable with RGB
            if (fallback != null) {
                fallback.convert(channelNames, channelGetter, stacksOut, logger);
                return;
            } else {
                throw new AnchorIOException("Cannot convert as its channels do not look like RGB");
            }
        }

        try {
            Stack stack = createRGBStack(channelGetter, logger.messageLogger());

            // The name is blank as there is a single channel
            stacksOut.accept("", stack);
        } catch (CreateException e) {
            throw new AnchorIOException("Incorrect image size", e);
        }
    }

    private static Stack createRGBStack(
            ChannelGetterForTimepoint channelGetter, MessageLogger logger) throws CreateException {

        Stack stackRearranged = new Stack();

        addChannelOrBlank("red", channelGetter, stackRearranged, logger);
        addChannelOrBlank("green", channelGetter, stackRearranged, logger);
        addChannelOrBlank("blue", channelGetter, stackRearranged, logger);

        return stackRearranged;
    }

    private static void addChannelOrBlank(
            String channelName,
            ChannelGetterForTimepoint channelGetter,
            Stack stackRearranged,
            MessageLogger logger)
            throws CreateException {
        try {
            if (channelGetter.hasChannel(channelName)) {
                stackRearranged.addChannel(channelGetter.getChannel(channelName));
            } else {
                logger.logFormatted(String.format("Adding a blank channel for %s", channelName));
                stackRearranged.addBlankChannel();
            }
        } catch (IncorrectImageSizeException
                | OperationFailedException
                | GetOperationFailedException e) {
            throw new CreateException(e);
        }
    }

    private static boolean channelNamesAreRGB(Set<String> channelNames) {
        if (channelNames.size() > 3) {
            return false;
        }

        for (String key : channelNames) {
            // If a key doesn't match one of the expected red-green-blue names
            if (!(key.equals("red") || key.equals("green") || key.equals("blue"))) {
                return false;
            }
        }

        return true;
    }
}
