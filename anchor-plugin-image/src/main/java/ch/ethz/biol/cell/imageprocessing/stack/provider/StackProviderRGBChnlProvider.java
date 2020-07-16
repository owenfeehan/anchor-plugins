/* (C)2020 */
package ch.ethz.biol.cell.imageprocessing.stack.provider;

import org.anchoranalysis.bean.BeanInstanceMap;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.annotation.OptionalBean;
import org.anchoranalysis.bean.error.BeanMisconfiguredException;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.image.bean.provider.ChnlProvider;
import org.anchoranalysis.image.bean.provider.stack.StackProvider;
import org.anchoranalysis.image.channel.Channel;
import org.anchoranalysis.image.channel.factory.ChannelFactory;
import org.anchoranalysis.image.extent.ImageDimensions;
import org.anchoranalysis.image.extent.IncorrectImageSizeException;
import org.anchoranalysis.image.stack.Stack;
import org.anchoranalysis.image.voxel.datatype.VoxelDataType;
import org.anchoranalysis.image.voxel.datatype.VoxelDataTypeUnsignedByte;

public class StackProviderRGBChnlProvider extends StackProvider {

    // START BEAN PROPERTIES
    @BeanField @OptionalBean private ChnlProvider red;

    @BeanField @OptionalBean private ChnlProvider green;

    @BeanField @OptionalBean private ChnlProvider blue;
    // END BEAN PROPERTIES

    @Override
    public void checkMisconfigured(BeanInstanceMap defaultInstances)
            throws BeanMisconfiguredException {
        super.checkMisconfigured(defaultInstances);

        if (red == null && green == null && blue == null) {
            throw new BeanMisconfiguredException(
                    "At least one of the chnlProviderRed, chnlProviderGreen or chnlProviderBlue must be set");
        }
    }

    private static ImageDimensions combineWithExisting(ImageDimensions existing, Channel toCombine)
            throws IncorrectImageSizeException {

        if (toCombine == null) {
            return existing;
        }

        if (existing == null) {
            return toCombine.getDimensions();
        }

        if (!existing.equals(toCombine.getDimensions())) {
            throw new IncorrectImageSizeException("dims are not equal");
        }

        return existing;
    }

    private static ImageDimensions createDimensions(
            Channel chnlRed, Channel chnlGreen, Channel chnlBlue)
            throws IncorrectImageSizeException {

        if (chnlRed == null && chnlGreen == null && chnlBlue == null) {
            throw new IllegalArgumentException("All chnls are null");
        }

        ImageDimensions sd = null;
        sd = combineWithExisting(sd, chnlRed);
        sd = combineWithExisting(sd, chnlGreen);
        sd = combineWithExisting(sd, chnlBlue);
        return sd;
    }

    private static void addToStack(
            Stack stack, Channel chnl, ImageDimensions dimensions, VoxelDataType outputChnlType)
            throws IncorrectImageSizeException, CreateException {

        if (chnl == null) {
            chnl = ChannelFactory.instance().createEmptyInitialised(dimensions, outputChnlType);
        }

        if (!outputChnlType.equals(chnl.getVoxelDataType())) {
            throw new CreateException(
                    String.format(
                            "Channel has a different type (%s) that the expected output-type (%s)",
                            chnl.getVoxelDataType(), outputChnlType));
        }

        stack.addChnl(chnl);
    }

    private static String voxelDataTypeString(Channel chnl) {
        if (chnl != null) {
            return chnl.getVoxelDataType().toString();
        } else {
            return ("empty");
        }
    }

    // Chooses the output type of the data
    private static VoxelDataType chooseOutputDataType(
            Channel chnlRed, Channel chnlGreen, Channel chnlBlue) throws CreateException {

        VoxelDataType dataType = null;

        Channel[] all = new Channel[] {chnlRed, chnlGreen, chnlBlue};

        for (Channel c : all) {
            if (c == null) {
                continue;
            }

            if (dataType == null) {
                dataType = c.getVoxelDataType();
            } else {
                if (!c.getVoxelDataType().equals(dataType)) {
                    String s =
                            String.format(
                                    "Input channels have different voxel data types. Red=%s; Green=%s; Blue=%s",
                                    voxelDataTypeString(chnlRed),
                                    voxelDataTypeString(chnlGreen),
                                    voxelDataTypeString(chnlBlue));
                    throw new CreateException(s);
                }
            }
        }

        // If we have no channels, then default to unsigned 8-bit
        if (dataType == null) {
            dataType = VoxelDataTypeUnsignedByte.INSTANCE;
        }

        return dataType;
    }

    @Override
    public Stack create() throws CreateException {

        Channel chnlRed = red != null ? red.create() : null;
        Channel chnlGreen = green != null ? green.create() : null;
        Channel chnlBlue = blue != null ? blue.create() : null;

        VoxelDataType outputType = chooseOutputDataType(chnlRed, chnlGreen, chnlBlue);

        return createRGBStack(chnlRed, chnlGreen, chnlBlue, outputType);
    }

    public static Stack createRGBStack(
            Channel chnlRed, Channel chnlGreen, Channel chnlBlue, VoxelDataType outputType)
            throws CreateException {
        try {
            ImageDimensions sd = createDimensions(chnlRed, chnlGreen, chnlBlue);

            Stack out = new Stack();
            addToStack(out, chnlRed, sd, outputType);
            addToStack(out, chnlGreen, sd, outputType);
            addToStack(out, chnlBlue, sd, outputType);
            return out;
        } catch (IncorrectImageSizeException e) {
            throw new CreateException(e);
        }
    }

    public ChnlProvider getRed() {
        return red;
    }

    public void setRed(ChnlProvider red) {
        this.red = red;
    }

    public ChnlProvider getGreen() {
        return green;
    }

    public void setGreen(ChnlProvider green) {
        this.green = green;
    }

    public ChnlProvider getBlue() {
        return blue;
    }

    public void setBlue(ChnlProvider blue) {
        this.blue = blue;
    }
}
