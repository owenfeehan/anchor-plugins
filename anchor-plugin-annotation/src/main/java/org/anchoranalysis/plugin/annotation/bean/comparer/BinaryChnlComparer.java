/* (C)2020 */
package org.anchoranalysis.plugin.annotation.bean.comparer;

import java.nio.file.Path;
import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.annotation.io.bean.comparer.Comparer;
import org.anchoranalysis.annotation.io.wholeimage.findable.Findable;
import org.anchoranalysis.annotation.io.wholeimage.findable.Found;
import org.anchoranalysis.annotation.io.wholeimage.findable.NotFound;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.annotation.DefaultInstance;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.image.binary.mask.Mask;
import org.anchoranalysis.image.binary.values.BinaryValues;
import org.anchoranalysis.image.extent.ImageDimensions;
import org.anchoranalysis.image.io.RasterIOException;
import org.anchoranalysis.image.io.bean.rasterreader.RasterReader;
import org.anchoranalysis.image.io.bean.rasterreader.RasterReaderUtilities;
import org.anchoranalysis.image.object.ObjectCollection;
import org.anchoranalysis.image.object.ObjectCollectionFactory;
import org.anchoranalysis.io.bean.filepath.generator.FilePathGenerator;
import org.anchoranalysis.io.error.AnchorIOException;

public class BinaryChnlComparer extends Comparer {

    // START BEAN PROPERTIES
    @BeanField @Getter @Setter private FilePathGenerator filePathGenerator;

    @BeanField @DefaultInstance @Getter @Setter private RasterReader rasterReader;

    @BeanField @Getter @Setter private boolean invert = false;
    // END BEAN PROPERTIES

    @Override
    public Findable<ObjectCollection> createObjects(
            Path filePathSource, ImageDimensions dimensions, boolean debugMode)
            throws CreateException {

        try {
            Path maskPath = filePathGenerator.outFilePath(filePathSource, debugMode);

            if (!maskPath.toFile().exists()) {
                return new NotFound<>(maskPath, "No mask exists");
            }

            Mask chnl =
                    RasterReaderUtilities.openBinaryChnl(
                            rasterReader, maskPath, createBinaryValues());

            return new Found<>(convertToObjects(chnl));

        } catch (AnchorIOException | RasterIOException e) {
            throw new CreateException(e);
        }
    }

    private BinaryValues createBinaryValues() {
        if (invert) {
            return BinaryValues.getDefault().createInverted();
        } else {
            return BinaryValues.getDefault();
        }
    }

    private static ObjectCollection convertToObjects(Mask chnl) {
        return ObjectCollectionFactory.from(chnl);
    }
}
