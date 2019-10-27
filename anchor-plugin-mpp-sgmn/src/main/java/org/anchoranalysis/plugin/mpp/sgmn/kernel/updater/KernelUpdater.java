package org.anchoranalysis.plugin.mpp.sgmn.kernel.updater;

/*-
 * #%L
 * anchor-plugin-mpp-sgmn
 * %%
 * Copyright (C) 2010 - 2019 Owen Feehan, ETH Zurich, University of Zurich, Hoffmann la Roche
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

import org.anchoranalysis.mpp.sgmn.bean.kernel.Kernel;
import org.anchoranalysis.plugin.mpp.sgmn.optscheme.TransformationContext;

import ch.ethz.biol.cell.mpp.pair.UpdateMarkSetException;

/**
 * 
 * @author FEEHANO
 *
 * @param <S> kernel type
 * @param <T> state-type
 */
public abstract class KernelUpdater<S,T> {
	
	/**
	 * Informs that a proposal from a particular kernel has been accepted
	 * 
	 * @param kernel the kernel whose proposal was accepted
	 * @param crnt the existing state
	 * @param proposed the new state that was accepted
	 * @param context TODO
	 * @throws UpdateMarkSetException
	 */
	public abstract void kernelAccepted( Kernel<S> kernel, T crnt, T proposed, TransformationContext context ) throws UpdateMarkSetException;
}
