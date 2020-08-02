/*-
 * #%L
 * anchor-plugin-io
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

package org.anchoranalysis.plugin.io.bean.rasterreader;

import java.nio.file.Path;
import java.util.Optional;
import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.annotation.DefaultInstance;
import org.anchoranalysis.bean.annotation.NonNegative;
import org.anchoranalysis.bean.shared.relation.RelationBean;
import org.anchoranalysis.core.relation.RelationToValue;
import org.anchoranalysis.image.extent.ImageResolution;
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

    // START BEAN PROPERTIES
    @BeanField @DefaultInstance @Getter @Setter private RasterReader rasterReader;

    @BeanField @Getter @Setter private RelationBean relation;

    @BeanField @NonNegative @Getter @Setter private double value;
    // END BEAN PROPERTIES

    private static class MaybeRejectProcessor
            implements OpenedRasterAlterDimensions.ConsiderUpdatedImageResolution {

        private RelationToValue relation;
        private double value;

        public MaybeRejectProcessor(RelationToValue relation, double value) {
            super();
            this.relation = relation;
            this.value = value;
        }

        @Override
        public Optional<ImageResolution> maybeUpdatedResolution(ImageResolution res)
                throws RasterIOException {

            if (res.getX() != res.getY()) {
                throw new RasterIOException(
                        "X and Y pixel-sizes are different. They must be equal");
            }

            if (relation.isRelationToValueTrue(res.getX(), value)) {
                throw new RasterIOException(
                        "XY-resolution fufills condition, and is thus rejected");
            }

            return Optional.empty();
        }
    }

    @Override
    public OpenedRaster openFile(Path filepath) throws RasterIOException {
        OpenedRaster or = rasterReader.openFile(filepath);
        return new OpenedRasterAlterDimensions(
                or, new MaybeRejectProcessor(relation.create(), value));
    }
}
