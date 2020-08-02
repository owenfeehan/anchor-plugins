package org.anchoranalysis.plugin.image.bean.thumbnail.object;

import java.util.Optional;
import org.anchoranalysis.bean.AnchorBean;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.image.object.ObjectCollection;
import org.anchoranalysis.image.stack.DisplayStack;
import org.anchoranalysis.image.stack.Stack;

/**
 * Creates a thumbnail of one or more objects on a stack by drawing the outline of the objects
 * 
 * @author Owen Feehan
 *
 */
public abstract class ThumbnailFromObjects extends AnchorBean<ThumbnailFromObjects> {
    
    /**
     * Initializes the thumbnail creator
     * <p>
     * Should always be called once before any calls to {@link #thumbnailFor}
     * @param objects the entire set of objects for which thumbnails may be subsequently created
     * @param background a stack that will be used to form the background (or some part of may be used)
     */
    public abstract void start(ObjectCollection objects, Optional<Stack> background);
    
    /** Creates a thumbnail for one or more objects */
    public abstract DisplayStack thumbnailFor(ObjectCollection objects) throws CreateException;
}
