/* (C)2020 */
package ch.ethz.biol.cell.imageprocessing.objmask.provider.smoothspline;

import java.util.List;
import java.util.function.ToIntFunction;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.core.geometry.Point3f;
import org.anchoranalysis.core.geometry.Point3i;
import org.anchoranalysis.image.contour.Contour;
import org.anchoranalysis.image.object.ObjectMask;
import org.anchoranalysis.plugin.opencv.CVFindContours;
import umontreal.ssj.functionfit.SmoothingCubicSpline;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class SplitContourSmoothingSpline {

    /**
     * 1. Fits a smoothed-spline to the contour 2. Splits this at each optima (critical points i.e.
     * zero-crossing of first-derivative)
     *
     * @param object object-mask representing a closed contour
     * @param rho smoothing factor [0,1] as per SSJ documentation
     * @param minimumNumberPoints if less than this number of points, object is returned unchanged
     * @return
     * @throws OperationFailedException
     */
    public static ContourList apply(
            ObjectMask object, double rho, int numberLoopPoints, int minimumNumberPoints)
            throws OperationFailedException {

        return traversePointsAndCallFitter(
                object,
                minimumNumberPoints,
                (pts, out) -> fitSplinesAndExtract(pts, rho, pts, numberLoopPoints, out));
    }

    private static ContourList traversePointsAndCallFitter(
            ObjectMask object, int minimumNumberPoints, FitSplinesExtract fitter)
            throws OperationFailedException {

        List<Contour> contoursTraversed = CVFindContours.contoursForObject(object);

        ContourList out = new ContourList();

        for (Contour contour : contoursTraversed) {
            addSplinesFor(contour, out, fitter, minimumNumberPoints);
        }

        return out;
    }

    private static void addSplinesFor(
            Contour contourIn,
            ContourList contoursOut,
            FitSplinesExtract fitter,
            int minNumPoints) {
        if (contourIn.getPoints().size() < minNumPoints) {
            contoursOut.add(contourIn);
        } else {
            fitter.apply(contourIn.pointsDiscrete(), contoursOut);
        }
    }

    @FunctionalInterface
    private static interface FitSplinesExtract {
        public void apply(List<Point3i> pointsTraversed, ContourList out);
    }

    /**
     * Fits splines to the pts in ptsTraversed (and maybe some points from ptsExtra)
     *
     * <p>As it can be useful to have some overlap with another contour, or to the beginning of the
     * same contour (to handle cyclical contours) ptsExtra provides a means to append some
     * additional points to be fit against.
     *
     * @param pointsToFit the points to fit the splines to
     * @param rho a smoothing factor [0, 1] see SSJ documentation
     * @param pointsExtra additional points, of which the first numExtraPoints will be appended to
     *     ptsToFit
     * @param numExtraPoints how many additional points to include
     * @param out created contours are appended to the list
     */
    private static void fitSplinesAndExtract(
            List<Point3i> pointsToFit,
            double rho,
            List<Point3i> pointsExtra,
            int numExtraPoints,
            ContourList out) {
        if (numExtraPoints > pointsExtra.size()) {
            numExtraPoints = pointsExtra.size();
        }

        double[] u = integerSequence(pointsToFit.size() + numExtraPoints);
        double[] x = extractFromPoint(pointsToFit, Point3i::getX, pointsExtra, numExtraPoints);
        double[] y = extractFromPoint(pointsToFit, Point3i::getY, pointsExtra, numExtraPoints);

        // We use two smoothing splines with respect to artificial parameter u (just an integer
        // sequence)
        extractSplitContour(
                new SmoothingCubicSpline(u, x, rho),
                new SmoothingCubicSpline(u, y, rho),
                u.length,
                out);
    }

    /** Extracts and splits contours from the splines */
    private static ContourList extractSplitContour(
            SmoothingCubicSpline splineX,
            SmoothingCubicSpline splineY,
            int maxEvalPoint,
            ContourList out) {
        double prevDer = Double.NaN;

        Contour contour = new Contour();

        double step = 0.05;
        double z = 0;
        while (z <= maxEvalPoint) {

            double xEval = splineX.evaluate(z);
            double yEval = splineY.evaluate(z);

            double xDer = splineX.derivative(z);
            double yDer = splineY.derivative(z);

            double der = xDer / yDer;

            if (!Double.isNaN(prevDer)
                    && !Double.isNaN(der)
                    && Math.signum(der) != Math.signum(prevDer)) {
                out.add(contour);
                contour = new Contour();
            }
            prevDer = der;

            contour.getPoints().add(new Point3f((float) xEval, (float) yEval, 0.0f));

            z += step;
        }

        out.add(contour);
        return out;
    }

    /**
     * @param points
     * @param extracter
     * @param numExtraPoints repeats the first numLoopedPoints points again at end of curve (to help
     *     deal with closed curves)
     * @return
     */
    private static double[] extractFromPoint(
            List<Point3i> points,
            ToIntFunction<Point3i> extracter,
            List<Point3i> pointsExtra,
            int numExtraPoints) {

        double[] out = new double[points.size() + numExtraPoints];

        for (int i = 0; i < points.size(); i++) {
            out[i] = (double) extracter.applyAsInt(points.get(i));
        }

        for (int i = 0; i < numExtraPoints; i++) {
            out[points.size() + i] = (double) extracter.applyAsInt(pointsExtra.get(i));
        }

        return out;
    }

    // Integer sequence starting at 0
    private static double[] integerSequence(int size) {
        double[] out = new double[size];

        for (int i = 0; i < size; i++) {
            out[i] = i;
        }

        return out;
    }
}
