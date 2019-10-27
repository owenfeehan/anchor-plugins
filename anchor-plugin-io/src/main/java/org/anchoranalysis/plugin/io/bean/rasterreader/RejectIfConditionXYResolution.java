package org.anchoranalysis.plugin.io.bean.rasterreader;

import java.nio.file.Path;

/*
 * #%L
 * anchor-io
 * %%
 * Copyright (C) 2016 ETH Zurich, University of Zurich, Owen Feehan
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
import org.anchoranalysis.bean.annotation.DefaultInstance;
import org.anchoranalysis.bean.annotation.NonNegative;
import org.anchoranalysis.bean.shared.relation.RelationBean;
import org.anchoranalysis.core.relation.RelationToValue;
import org.anchoranalysis.image.extent.ImageDim;
import org.anchoranalysis.image.extent.ImageRes;
import org.anchoranalysis.image.io.RasterIOException;
import org.anchoranalysis.image.io.bean.rasterreader.RasterReader;
import org.anchoranalysis.image.io.rasterreader.OpenedRaster;

// If the XY resolution of an opened-image meets a certain condition
//  then the resolution is scaled by a factor
//
// This is useful for correcting situations where there has been a unit
//  mixup by the reader
//
// Assumes the X and Y resolution are equal. Throws an error otherwise.
public class RejectIfConditionXYResolution extends RasterReader {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	// START BEAN PROPERTIES
	@BeanField @DefaultInstance
	private RasterReader rasterReader;
	
	@BeanField
	private RelationBean relation;
	
	@BeanField @NonNegative
	private double value;
	// END BEAN PROPERTIES
	
	private static class MaybeRejectProcessor implements OpenedRasterAlterDimensions.ProcessDimensions {

		private RelationToValue relation;
		private double value;
				
		public MaybeRejectProcessor(RelationToValue relation, double value) {
			super();
			this.relation = relation;
			this.value = value;
		}

		@Override
		public void maybeAlterDimensions(ImageDim sd) throws RasterIOException {
			
			ImageRes sr = sd.getRes();
			
			if (sr.getX()!=sr.getY()) {
				throw new RasterIOException("X and Y pixel-sizes are different. They must be equal");
			}
			
			if( relation.isRelationToValueTrue(sr.getX(), value) ) {
				throw new RasterIOException("XY-resolution fufills condition, and is thus rejected");
			}
			
		}
		
	}
	
	@Override
	public OpenedRaster openFile(Path filepath) throws RasterIOException {

		OpenedRaster or = rasterReader.openFile(filepath);
		return new OpenedRasterAlterDimensions(or,new MaybeRejectProcessor(relation.create(),value));
	}

	public RasterReader getRasterReader() {
		return rasterReader;
	}

	public void setRasterReader(RasterReader rasterReader) {
		this.rasterReader = rasterReader;
	}

	public RelationBean getRelation() {
		return relation;
	}

	public void setRelation(RelationBean relation) {
		this.relation = relation;
	}

	public double getValue() {
		return value;
	}

	public void setValue(double value) {
		this.value = value;
	}
}
