/* (C)2020 */
package ch.ethz.biol.cell.imageprocessing.chnl.provider;

import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.image.channel.Channel;
import org.anchoranalysis.image.channel.factory.ChannelFactoryByte;
import org.anchoranalysis.image.channel.factory.ChannelFactorySingleType;
import org.anchoranalysis.image.voxel.box.VoxelBox;
import org.anchoranalysis.image.voxel.buffer.VoxelBuffer;

/**
 * Base-class for operations that perform a binary arithmetic operation with each voxel-value and a
 * constant.
 *
 * @author Owen Feehan
 */
public abstract class ChnlProviderOneValueArithmetic extends ChnlProviderOneValue {

    private static final ChannelFactorySingleType FACTORY = new ChannelFactoryByte();

    @Override
    public Channel createFromChnlValue(Channel chnl, double value) throws CreateException {

        int constant = (int) value;

        Channel chnlOut = FACTORY.createEmptyInitialised(chnl.getDimensions());

        VoxelBox<?> vbIn = chnl.getVoxelBox().any();
        VoxelBox<?> vbOut = chnlOut.getVoxelBox().any();

        int volumeXY = chnl.getDimensions().getVolumeXY();

        for (int z = 0; z < chnl.getDimensions().getZ(); z++) {

            VoxelBuffer<?> in = vbIn.getPixelsForPlane(z);
            VoxelBuffer<?> out = vbOut.getPixelsForPlane(z);

            for (int offset = 0; offset < volumeXY; offset++) {

                int voxelVal = in.getInt(offset);

                int result = binaryOp(voxelVal, constant);

                out.putInt(offset, cropValToByteRange(result));
            }
        }

        return chnlOut;
    }

    /** The binary arithmetic operation that combines the voxel-value and the constant-value */
    protected abstract int binaryOp(int voxel, int constant);

    private static int cropValToByteRange(int result) {

        if (result < 0) {
            return 0;
        }

        if (result > 255) {
            return 255;
        }

        return result;
    }
}
