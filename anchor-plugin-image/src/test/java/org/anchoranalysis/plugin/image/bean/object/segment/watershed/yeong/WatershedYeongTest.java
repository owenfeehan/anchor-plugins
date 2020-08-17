/*-
 * #%L
 * anchor-plugin-image
 * %%
 * Copyright (C) 2010 - 2020 Owen Feehan, ETH Zurich, University of Zurich, Hoffmann-La Roche
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

package org.anchoranalysis.plugin.image.bean.object.segment.watershed.yeong;

import static org.junit.Assert.*;

import java.util.Optional;
import org.anchoranalysis.image.bean.nonbean.error.SegmentationFailedException;
import org.anchoranalysis.image.binary.voxel.BinaryVoxelsFactory;
import org.anchoranalysis.image.channel.Channel;
import org.anchoranalysis.image.object.ObjectCollection;
import org.anchoranalysis.image.object.ObjectMask;
import org.anchoranalysis.io.output.error.OutputWriteFailedException;
import org.anchoranalysis.test.TestDataLoadException;
import org.anchoranalysis.test.TestLoader;
import org.anchoranalysis.test.image.io.TestLoaderImageIO;
import org.junit.Test;

public class WatershedYeongTest {

    private static final String PATH_CHANNEL_BLURRED = "chnlInBlurred.tif";
    private static final String PATH_MASK = "mask.tif";

    private static final String PATH_EXPECTED_NO_MASKS_NO_SEEDS =
            "blurredResult_noMasks_noSeeds.h5";
    private static final String PATH_EXPECTED_MASKS_NO_SEEDS = "blurredResult_masks_noSeeds.h5";

    private TestLoaderImageIO loader =
            new TestLoaderImageIO(TestLoader.createFromMavenWorkingDirectory("watershed01/"));

    @Test
    public void test_noMasks_noSeeds()
            throws SegmentationFailedException, TestDataLoadException, OutputWriteFailedException {
        sgmn(PATH_EXPECTED_NO_MASKS_NO_SEEDS, Optional.empty());
    }

    @Test
    public void test_masks_noSeeds()
            throws SegmentationFailedException, TestDataLoadException, OutputWriteFailedException {
        sgmn(PATH_EXPECTED_MASKS_NO_SEEDS, Optional.of(PATH_MASK));
    }

    private void sgmn(String pathObjectsExpected, Optional<String> pathMask)
            throws SegmentationFailedException, TestDataLoadException, OutputWriteFailedException {
        WatershedYeong sgmn = new WatershedYeong();

        Optional<ObjectMask> objectMask = pathMask.map(this::maskAsObject);

        ObjectCollection objectsResult =
                sgmn.segment(channelFor(PATH_CHANNEL_BLURRED), objectMask, Optional.empty());

        ObjectCollection objectsExpected = loader.openObjectsFromTestPath(pathObjectsExpected);

        assertTrue(objectsExpected.equalsDeep(objectsResult));
    }

    private ObjectMask maskAsObject(String path) {
        return new ObjectMask(BinaryVoxelsFactory.reuseByte( channelFor(PATH_MASK).voxels().asByte() ));
    }

    private Channel channelFor(String path) {
        return loader.openChannelFromTestPath(path);
    }
}
