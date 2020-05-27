package org.anchoranalysis.plugin.image.bean.blur;

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

import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.core.log.LogReporter;
import org.anchoranalysis.image.convert.ImgLib2Wrap;
import org.anchoranalysis.image.extent.Extent;
import org.anchoranalysis.image.extent.ImageDim;
import org.anchoranalysis.image.voxel.box.VoxelBoxWrapper;

/**
 * Performs a Gaussian-blur in 2D on each slice independently
 * 
 * @author Owen Feehan
 *
 */
public class BlurGaussianEachSlice2d extends BlurStrategy {

	@Override
	public void blur( VoxelBoxWrapper voxelBox, ImageDim dim, LogReporter logger ) throws OperationFailedException {
		
		double sigma = calcSigma(dim, logger);
		
		Extent e = voxelBox.any().extent();
		double[] sigmaArr = new double[]{ sigma, sigma };
		
		for( int z=0; z<e.getZ(); z++) {

			GaussianBlurUtilities.applyBlur(
				ImgLib2Wrap.wrap( voxelBox.any().getPixelsForPlane(z), e ),
				dim.getRes(),
				sigmaArr
			);
		}
	}
}
