package org.anchoranalysis.gui.annotation.bean.label;

/*-
 * #%L
 * anchor-plugin-annotation
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

import java.util.Collection;
import java.util.Set;

import org.apache.commons.collections.MultiMap;
import org.apache.commons.collections.map.MultiValueMap;

/** A set of annotation labels partitoned into their unique groups */
public class GroupedAnnotationLabels {

	// <String,AnnotationLabel>
	private MultiMap map = new MultiValueMap();
	
	public GroupedAnnotationLabels( Collection<AnnotationLabel> labels ) {
		
		for( AnnotationLabel lab : labels ) {
			map.put( lab.getGroup(), lab );
		}
	}
	
	public int numGroups() {
		return map.keySet().size();
	}

	@SuppressWarnings("unchecked")
	public Set<String> keySet() {
		return (Set<String>) map.keySet();
	}
	
	@SuppressWarnings("unchecked")
	public Collection<AnnotationLabel> get(String key) {
		return (Collection<AnnotationLabel>) map.get(key);
	}
}
