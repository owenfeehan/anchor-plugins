package org.anchoranalysis.plugin.image.bean.chnl.level;

/*-
 * #%L
 * anchor-plugin-image
 * %%
 * Copyright (C) 2010 - 2020 Owen Feehan
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

import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.image.bean.provider.ChnlProvider;
import org.anchoranalysis.image.bean.provider.ChnlProviderOne;
import org.anchoranalysis.image.bean.provider.ObjectCollectionProvider;
import org.anchoranalysis.image.bean.threshold.CalculateLevel;
import org.anchoranalysis.image.channel.Channel;
import org.anchoranalysis.image.object.ObjectCollection;

import ch.ethz.biol.cell.imageprocessing.chnl.provider.DimChecker;
import lombok.Getter;
import lombok.Setter;

public abstract class ChnlProviderLevel extends ChnlProviderOne {

	// START BEAN PROPERTIES
	/** The input (an intensity channel in the normal way) */
	@BeanField @Getter @Setter
	private ObjectCollectionProvider objs;
	
	@BeanField @Getter @Setter
	private ChnlProvider chnlOutput;
	
	@BeanField @Getter @Setter
	private CalculateLevel calculateLevel;
	// END BEAN PROPERTIES
	
	@Override
	public Channel createFromChnl(Channel chnl) throws CreateException {
				
		return createFor(
			chnl,
			objs.create(),
			DimChecker.createSameSize(chnlOutput, "chnlOutput", chnl)
			
		);
	}
	
	protected abstract Channel createFor( Channel chnlIntensity, ObjectCollection objs, Channel chnlOutput ) throws CreateException;
}
