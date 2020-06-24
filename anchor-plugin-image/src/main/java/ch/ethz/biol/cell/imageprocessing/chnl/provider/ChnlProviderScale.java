package ch.ethz.biol.cell.imageprocessing.chnl.provider;

/*
 * #%L
 * anchor-plugin-image
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
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.core.log.LogReporter;
import org.anchoranalysis.image.bean.interpolator.InterpolatorBean;
import org.anchoranalysis.image.bean.interpolator.InterpolatorBeanLanczos;
import org.anchoranalysis.image.bean.provider.ChnlProviderOne;
import org.anchoranalysis.image.bean.scale.ScaleCalculator;
import org.anchoranalysis.image.channel.Channel;
import org.anchoranalysis.image.interpolator.Interpolator;
import org.anchoranalysis.image.scale.ScaleFactor;

public class ChnlProviderScale extends ChnlProviderOne {

	// Start BEAN PROPERTIES
	@BeanField
	private ScaleCalculator scaleCalculator;
	
	@BeanField
	private InterpolatorBean interpolator = new InterpolatorBeanLanczos();
	// End BEAN PROPERTIES
	
	@Override
	public Channel createFromChnl(Channel chnl) throws CreateException {
		return scale(
			chnl,
			scaleCalculator,
			interpolator.create(),
			getLogger().getLogReporter()
		);
	}
	
	public static Channel scale( Channel chnl, ScaleCalculator scaleCalculator, Interpolator interpolator, LogReporter logger) throws CreateException {
		try {
			logger.logFormatted("incoming Image Resolution: %s\n", chnl.getDimensions().getRes() );
			
			ScaleFactor sf = scaleCalculator.calc( chnl.getDimensions() );
			
			logger.logFormatted("Scale Factor: %s\n", sf.toString() );
			
			Channel chnlOut = chnl.scaleXY( sf.getX(), sf.getY(), interpolator);
			
			logger.logFormatted("outgoing Image Resolution: %s\n", chnlOut.getDimensions().getRes() );
			
			return chnlOut;
			
		} catch (OperationFailedException e) {
			throw new CreateException(e);
		}
	}
	
	public ScaleCalculator getScaleCalculator() {
		return scaleCalculator;
	}

	public void setScaleCalculator(ScaleCalculator scaleCalculator) {
		this.scaleCalculator = scaleCalculator;
	}

	public InterpolatorBean getInterpolator() {
		return interpolator;
	}

	public void setInterpolator(InterpolatorBean interpolator) {
		this.interpolator = interpolator;
	}
}
