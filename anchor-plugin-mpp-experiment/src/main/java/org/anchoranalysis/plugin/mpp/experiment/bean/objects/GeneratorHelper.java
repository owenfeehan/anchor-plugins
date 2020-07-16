/* (C)2020 */
package org.anchoranalysis.plugin.mpp.experiment.bean.objects;

import java.awt.Color;
import java.util.function.Function;
import lombok.AllArgsConstructor;
import org.anchoranalysis.bean.StringSet;
import org.anchoranalysis.core.color.ColorIndex;
import org.anchoranalysis.core.color.ColorList;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.name.provider.NamedProviderGetException;
import org.anchoranalysis.core.name.value.SimpleNameValue;
import org.anchoranalysis.image.extent.BoundingBox;
import org.anchoranalysis.image.extent.ImageDimensions;
import org.anchoranalysis.image.io.generator.raster.bbox.ExtractedBoundingBoxGenerator;
import org.anchoranalysis.image.io.generator.raster.bbox.ExtractedObjectGenerator;
import org.anchoranalysis.image.io.generator.raster.obj.ObjWithBoundingBoxGenerator;
import org.anchoranalysis.image.io.generator.raster.obj.rgb.DrawObjectsGenerator;
import org.anchoranalysis.image.object.ObjectMask;
import org.anchoranalysis.image.stack.NamedImgStackCollection;
import org.anchoranalysis.image.stack.Stack;
import org.anchoranalysis.io.bean.object.writer.Outline;
import org.anchoranalysis.io.generator.IterableGenerator;
import org.anchoranalysis.io.generator.IterableGeneratorBridge;
import org.anchoranalysis.io.generator.IterableObjectGenerator;
import org.anchoranalysis.io.generator.combined.IterableCombinedListGenerator;

@AllArgsConstructor
class GeneratorHelper {

    private final int outlineWidth;
    private final StringSet outputRGBOutline;
    private final StringSet outputRGBOutlineMIP;

    public IterableCombinedListGenerator<ObjectMask> buildGenerator(
            ImageDimensions dimensions,
            NamedImgStackCollection stacks,
            NamedImgStackCollection stacksFlattened,
            Function<Stack, ExtractedBoundingBoxGenerator> generatorFunction)
            throws CreateException {
        IterableCombinedListGenerator<ObjectMask> out =
                new IterableCombinedListGenerator<>(
                        new SimpleNameValue<>(
                                "mask", new ObjWithBoundingBoxGenerator(dimensions.getRes())));

        ColorList colors = new ColorList(Color.GREEN);
        try {
            addGeneratorForEachKey(stacks, colors, generatorFunction, out, false);
            addGeneratorForEachKey(stacksFlattened, colors, generatorFunction, out, true);
        } catch (NamedProviderGetException e) {
            throw new CreateException(e);
        }
        return out;
    }

    private void addGeneratorForEachKey(
            NamedImgStackCollection stacks,
            ColorList colors,
            Function<Stack, ExtractedBoundingBoxGenerator> generatorFunction,
            IterableCombinedListGenerator<ObjectMask> out,
            boolean mip)
            throws NamedProviderGetException {

        for (String key : stacks.keys()) {
            Stack stack = stacks.getException(key);

            ExtractedBoundingBoxGenerator generator = generatorFunction.apply(stack);

            out.add(key, wrapBBoxGenerator(generator, mip));

            if (!mip) {
                maybeOutput(outputRGBOutline, key, "_RGBOutline", generator, colors, out);
            }

            maybeOutput(outputRGBOutlineMIP, key, "_RGBOutlineMIP", generator, colors, out);
        }
    }

    private void maybeOutput(
            StringSet output,
            String key,
            String suffix,
            IterableObjectGenerator<BoundingBox, Stack> generator,
            ColorList colors,
            IterableCombinedListGenerator<ObjectMask> out) {
        if (output.contains(key)) {
            out.add(key + suffix, createExtractedObjectGenerator(generator, colors, true));
        }
    }

    private IterableGenerator<ObjectMask> createExtractedObjectGenerator(
            IterableObjectGenerator<BoundingBox, Stack> generator,
            ColorIndex colorIndex,
            boolean mip) {
        return new ExtractedObjectGenerator(
                new DrawObjectsGenerator(new Outline(outlineWidth), colorIndex),
                generator,
                "rgbOutline",
                mip);
    }

    private static IterableGenerator<ObjectMask> wrapBBoxGenerator(
            IterableGenerator<BoundingBox> generator, boolean mip) {
        return new IterableGeneratorBridge<>(
                generator, sourceObject -> boundingBoxFromObject(sourceObject, mip));
    }

    private static BoundingBox boundingBoxFromObject(ObjectMask object, boolean mip) {
        if (mip) {
            return object.getBoundingBox().flattenZ();
        } else {
            return object.getBoundingBox();
        }
    }
}
