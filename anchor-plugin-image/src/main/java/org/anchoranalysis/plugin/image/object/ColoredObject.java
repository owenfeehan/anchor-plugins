/*-
 * #%L
 * anchor-plugin-image
 * %%
 * Copyright (C) 2010 - 2022 Owen Feehan, ETH Zurich, University of Zurich, Hoffmann-La Roche
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
package org.anchoranalysis.plugin.image.object;

import org.anchoranalysis.core.color.RGBColor;
import org.anchoranalysis.image.voxel.object.ObjectMask;

import lombok.AllArgsConstructor;
import lombok.Value;

/**
 * An {@link ObjectMask} with an associated color.
 * 
 * @author Owen Feehan
 */
@Value @AllArgsConstructor
public class ColoredObject {

	/** The object-mask. */
	private ObjectMask object;

	/** The RGB color. */
	private RGBColor color;
	
    /**
     * A maximum-intensity projection.
     *
     * <p>This flattens across z-dimension, setting a voxel to <i>on</i> if it is <i>on</i> in any
     * one slice.
     *
     * <p>This is an <b>immutable</b> operation.
     *
     * @return a new {@link ColoredObject} flattened in Z dimension.
     */
	public ColoredObject flattenZ() {
		return new ColoredObject(object.flattenZ(), color);
	}
}
