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

package ch.ethz.biol.cell.imageprocessing.stack.provider;

import ch.ethz.biol.cell.imageprocessing.stack.color.ColoredObjectsStackCreator;
import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.anchor.overlay.bean.DrawObject;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.color.ColorList;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.image.object.ObjectCollection;
import org.anchoranalysis.image.object.ObjectMask;
import org.anchoranalysis.image.stack.DisplayStack;
import org.anchoranalysis.image.stack.Stack;
import org.anchoranalysis.io.bean.object.writer.Filled;
import org.anchoranalysis.io.bean.object.writer.Outline;

public abstract class StackProviderRGBFromObjectBase extends StackProviderWithBackground {

    // START BEAN PROPERTIES
    @BeanField @Getter @Setter private boolean outline = false;

    @BeanField @Getter @Setter private int outlineWidth = 1;

    @BeanField @Getter @Setter private boolean force2D = false;
    // END BEAN PROPERTIES

    protected Stack createStack(ObjectCollection objects, ColorList colors) throws CreateException {
        return ColoredObjectsStackCreator.create(
                maybeFlatten(objects),
                outline,
                outlineWidth,
                force2D,
                maybeFlattenedBackground(),
                colors);
    }

    protected DisplayStack maybeFlattenedBackground() throws CreateException {
        return backgroundStack(!force2D);
    }

    protected ObjectCollection maybeFlatten(ObjectCollection objects) {
        if (force2D) {
            return objects.stream().map(ObjectMask::flattenZ);
        } else {
            return objects;
        }
    }

    protected DrawObject createDrawer() {
        if (outline) {
            return new Outline(outlineWidth, force2D);
        } else {
            return new Filled();
        }
    }
}
