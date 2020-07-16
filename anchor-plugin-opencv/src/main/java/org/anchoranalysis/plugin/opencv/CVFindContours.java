/* (C)2020 */
package org.anchoranalysis.plugin.opencv;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.core.functional.FunctionalList;
import org.anchoranalysis.core.geometry.Point3f;
import org.anchoranalysis.core.geometry.ReadableTuple3i;
import org.anchoranalysis.image.contour.Contour;
import org.anchoranalysis.image.object.ObjectMask;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.imgproc.Imgproc;

/** Wrapper around Open CV's findContours function */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CVFindContours {

    static {
        CVInit.alwaysExecuteBeforeCallingLibrary();
    }

    public static List<Contour> contoursForObject(ObjectMask object)
            throws OperationFailedException {

        try {
            // We clone ss the source image is modified by the algorithm according to OpenCV docs
            // https://docs.opencv.org/2.4/modules/imgproc/doc/structural_analysis_and_shape_descriptors.html?highlight=findcontours#findcontours
            Mat mat = MatConverter.fromObject(object.duplicate());

            List<MatOfPoint> contours = new ArrayList<>();
            Imgproc.findContours(
                    mat, contours, new Mat(), Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_NONE);

            return convertMatOfPoint(contours, object.getBoundingBox().cornerMin());

        } catch (CreateException e) {
            throw new OperationFailedException(e);
        }
    }

    private static List<Contour> convertMatOfPoint(
            List<MatOfPoint> contours, ReadableTuple3i cornerMin) {
        return FunctionalList.mapToList(
                contours, points -> CVFindContours.createContour(points, cornerMin));
    }

    private static Contour createContour(MatOfPoint mop, ReadableTuple3i cornerMin) {
        Contour contour = new Contour();

        Arrays.stream(mop.toArray())
                .map(point -> convert(point, cornerMin))
                .forEach(contour.getPoints()::add);

        return contour;
    }

    private static Point3f convert(Point point, ReadableTuple3i cornerMin) {
        return new Point3f(
                convertAdd(point.x, cornerMin.getX()),
                convertAdd(point.y, cornerMin.getY()),
                cornerMin.getZ());
    }

    private static float convertAdd(double in, double add) {
        return (float) (in + add);
    }
}
