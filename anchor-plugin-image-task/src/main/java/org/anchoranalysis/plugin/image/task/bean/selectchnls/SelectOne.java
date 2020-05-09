package org.anchoranalysis.plugin.image.task.bean.selectchnls;

/*-
 * #%L
 * anchor-plugin-image-task
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

import java.util.ArrayList;
import java.util.List;

import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.image.chnl.Chnl;
import org.anchoranalysis.plugin.image.task.grouped.ChnlSource;
import org.anchoranalysis.plugin.image.task.grouped.NamedChnl;

/**
 * Selects one specific channel from a set of stacks
 * 
 * @author FEEHANO
 *
 */
public class SelectOne extends SelectChnlsFromStacks {

	// START BEAN FIELDS
	/** If defined, processing only occurs the stack with this specific stack and index. Otherwise processing occurs on all input stacks */
	@BeanField
	private String stackName;
	
	@BeanField
	private int chnlIndex = 0;
	// END BEAN FIELDS
	
	@Override
	public List<NamedChnl> selectChnls(ChnlSource source, boolean checkType) throws OperationFailedException {
		Chnl chnl = source.extractChnl(stackName, checkType, chnlIndex);
		
		List<NamedChnl> out = new ArrayList<>();
		out.add( 
			new NamedChnl(stackName,chnl)
		);
		return out;
	}

}
