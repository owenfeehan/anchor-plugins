/*-
 * #%L
 * anchor-plugin-annotation
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

package org.anchoranalysis.plugin.annotation.bean.comparison.assigner;

import java.nio.file.Path;
import java.util.List;
import org.anchoranalysis.annotation.io.assignment.ObjectCollectionDistanceMatrix;
import org.anchoranalysis.core.functional.FunctionalList;
import org.anchoranalysis.core.text.TypedValue;
import org.anchoranalysis.image.object.ObjectCollection;
import org.anchoranalysis.io.error.AnchorIOException;
import org.anchoranalysis.io.generator.tabular.CSVGenerator;
import org.anchoranalysis.io.output.bean.OutputWriteSettings;
import org.anchoranalysis.io.output.csv.CSVWriter;
import org.anchoranalysis.io.output.error.OutputWriteFailedException;

/**
 * Generates a CSV-file that is a table of distances between objects.
 * 
 * <p>The distance is between objects is providerd via a {@link ObjectCollectionDistanceMatrix}.
 * 
 * @author Owen Feehan
 *
 */
class ObjectsDistanceMatrixGenerator extends CSVGenerator<ObjectCollectionDistanceMatrix> {

    private static final String MANIFEST_FUNCTION = "objectCollectionDistanceMatrix";

    private final int numberDecimalPlaces;

    /**
     * Creates the generator.
     * 
     * @param distanceMatrix a matrix defining distance between the objects
     * @param numberDecimalPlaces the number of decimal places to use for the distance
     */
    public ObjectsDistanceMatrixGenerator(
            ObjectCollectionDistanceMatrix distanceMatrix, int numberDecimalPlaces) {
        super(MANIFEST_FUNCTION);
        assignElement(distanceMatrix);
        this.numberDecimalPlaces = numberDecimalPlaces;
    }

    @Override
    public void writeToFile(OutputWriteSettings outputWriteSettings, Path filePath)
            throws OutputWriteFailedException {

        ObjectCollectionDistanceMatrix distanceMatrix = getElement();
        
        try (CSVWriter csvWriter = CSVWriter.create(filePath)) {

            csvWriter.writeHeaders( headers(distanceMatrix.getObjects2()) );

            // The descriptions of objects1 go in the first column
            List<String> descriptions = descriptionFromObjects(distanceMatrix.getObjects1());

            // Write each row
            for (int i = 0; i < distanceMatrix.sizeObjects1(); i++) {
                csvWriter.writeRow( rowWithDescription(descriptions, i, distanceMatrix) );
            }
            
        } catch (AnchorIOException e) {
            throw new OutputWriteFailedException(e);
        }
    }

    /** A row including a description as first-column */
    private List<TypedValue> rowWithDescription(List<String> descriptions, int index, ObjectCollectionDistanceMatrix distanceMatrix) {
        List<TypedValue> row = rowWithoutDescription(index, distanceMatrix);

        // Insert the description as first column
        row.add(0, new TypedValue(descriptions.get(index)));
        
        return row;
    }

    /** A row excluding a description as first-column */
    private List<TypedValue> rowWithoutDescription(int index1, ObjectCollectionDistanceMatrix distanceMatrix) {
        return FunctionalList.mapRangeToList(0, distanceMatrix.sizeObjects2(), index2 ->
            new TypedValue(distanceMatrix.getDistance(index1, index2), numberDecimalPlaces)
        );
    }
    
    /** A sensible header string, bearing in the mind the first column has object descriptions. */
    private static List<String> headers(ObjectCollection objects) {
        List<String> headers = descriptionFromObjects(objects);
        headers.add(0, "Objects");
        return headers;
    }
    
    /** A description of each object in a collection. */
    private static List<String> descriptionFromObjects(ObjectCollection objects) {
        return objects.stream().mapToList(objectMask -> objectMask.centerOfGravity().toString());
    }
}
