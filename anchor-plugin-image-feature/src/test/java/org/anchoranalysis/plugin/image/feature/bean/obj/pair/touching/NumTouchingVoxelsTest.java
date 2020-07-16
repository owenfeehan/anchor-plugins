/* (C)2020 */
package org.anchoranalysis.plugin.image.feature.bean.obj.pair.touching;

import org.anchoranalysis.core.error.InitException;
import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.plugin.image.feature.bean.obj.pair.ParamsFixtureHelper;
import org.anchoranalysis.plugin.image.feature.bean.object.pair.touching.NumTouchingVoxels;
import org.junit.Test;

public class NumTouchingVoxelsTest {

    @Test
    public void testOverlapping() throws FeatureCalcException, InitException {

        ParamsFixtureHelper.testTwoSizesOverlappingDouble(new NumTouchingVoxels(), 91, 71);
    }
}
