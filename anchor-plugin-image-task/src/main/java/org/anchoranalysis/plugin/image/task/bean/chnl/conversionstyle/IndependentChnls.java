/* (C)2020 */
package org.anchoranalysis.plugin.image.task.bean.chnl.conversionstyle;

import java.util.Set;
import java.util.function.BiConsumer;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.index.GetOperationFailedException;
import org.anchoranalysis.core.log.Logger;
import org.anchoranalysis.image.channel.Channel;
import org.anchoranalysis.image.stack.Stack;
import org.anchoranalysis.io.error.AnchorIOException;
import org.anchoranalysis.plugin.image.task.chnl.convert.ChnlGetterForTimepoint;

public class IndependentChnls extends ChnlConversionStyle {

    // START BEAN PROPERTIES
    /** Iff TRUE and we cannot find a channel in the file, we ignore it and carry on */
    @BeanField private boolean ignoreMissingChnl = true;
    // END BEAN PROPERTIES

    public void convert(
            Set<String> chnlNames,
            ChnlGetterForTimepoint chnlGetter,
            BiConsumer<String, Stack> stacksOut,
            Logger logger)
            throws AnchorIOException {

        for (String key : chnlNames) {

            try {
                Channel chnl = chnlGetter.getChnl(key);
                stacksOut.accept(key, new Stack(chnl));
            } catch (GetOperationFailedException e) {
                if (ignoreMissingChnl) {
                    logger.messageLogger().logFormatted("Cannot open channel '%s'. Ignoring.", key);
                } else {
                    throw new AnchorIOException(String.format("Cannot open channel '%s'.", key), e);
                }
            }
        }
    }

    public boolean isIgnoreMissingChnl() {
        return ignoreMissingChnl;
    }

    public void setIgnoreMissingChnl(boolean ignoreMissingChnl) {
        this.ignoreMissingChnl = ignoreMissingChnl;
    }
}
