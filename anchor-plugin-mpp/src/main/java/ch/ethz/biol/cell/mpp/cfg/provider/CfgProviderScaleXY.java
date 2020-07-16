/* (C)2020 */
package ch.ethz.biol.cell.mpp.cfg.provider;

import org.anchoranalysis.anchor.mpp.bean.cfg.CfgProvider;
import org.anchoranalysis.anchor.mpp.cfg.Cfg;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.core.error.OptionalOperationUnsupportedException;
import org.anchoranalysis.image.bean.scale.ScaleCalculator;
import org.anchoranalysis.image.scale.ScaleFactor;

public class CfgProviderScaleXY extends CfgProvider {

    // START BEAN PROPERTIES
    @BeanField private CfgProvider cfgProvider;

    @BeanField private ScaleCalculator scaleCalculator;
    // END BEAN PROPERTIES

    @Override
    public Cfg create() throws CreateException {

        ScaleFactor sf;
        try {
            sf = scaleCalculator.calc(null);
        } catch (OperationFailedException e1) {
            throw new CreateException(e1);
        }

        if (Math.abs(sf.getX() - sf.getY()) >= 1e-1) {
            throw new CreateException(
                    String.format(
                            "ScaleFactor x=%f and y=%f should be within 1e-1 of each other",
                            sf.getX(), sf.getY()));
        }

        Cfg cfg = cfgProvider.create();

        Cfg cfgCopy = cfg.deepCopy();
        try {
            cfgCopy.scaleXY(sf.getX());
        } catch (OptionalOperationUnsupportedException e) {
            throw new CreateException(e);
        }
        return cfgCopy;
    }

    public CfgProvider getCfgProvider() {
        return cfgProvider;
    }

    public void setCfgProvider(CfgProvider cfgProvider) {
        this.cfgProvider = cfgProvider;
    }

    public ScaleCalculator getScaleCalculator() {
        return scaleCalculator;
    }

    public void setScaleCalculator(ScaleCalculator scaleCalculator) {
        this.scaleCalculator = scaleCalculator;
    }
}
