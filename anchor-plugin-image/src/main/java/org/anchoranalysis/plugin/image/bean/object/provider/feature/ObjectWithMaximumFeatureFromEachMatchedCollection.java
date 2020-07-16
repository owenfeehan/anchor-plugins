/* (C)2020 */
package org.anchoranalysis.plugin.image.bean.object.provider.feature;

import java.util.List;
import java.util.Optional;
import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.feature.session.calculator.FeatureCalculatorSingle;
import org.anchoranalysis.image.bean.object.ObjectMatcher;
import org.anchoranalysis.image.feature.object.input.FeatureInputSingleObject;
import org.anchoranalysis.image.object.MatchedObject;
import org.anchoranalysis.image.object.ObjectCollection;
import org.anchoranalysis.image.object.ObjectCollectionFactory;
import org.anchoranalysis.image.object.ObjectMask;

/**
 * Finds the object with the maximum feature among a group of matches for each object.
 *
 * <p>Specifically, for each object in the upstream collection, matches are found, and the object
 * from those matches with the highest feature value is selected for inclusion in the newly created
 * collection.
 *
 * <p>If every object has at least one match, the newly created collection will have as many
 * elements as the upstream collection.
 *
 * @author Owen Feehan
 */
public class ObjectWithMaximumFeatureFromEachMatchedCollection
        extends ObjectCollectionProviderWithFeature {

    // START BEAN PROPERTIES
    @BeanField @Getter @Setter private ObjectMatcher matcher;
    // END BEAN PROPERTIES

    @Override
    public ObjectCollection createFromObjects(ObjectCollection objects) throws CreateException {

        FeatureCalculatorSingle<FeatureInputSingleObject> session = createSession();

        try {
            List<MatchedObject> listMatches = matcher.findMatch(objects);

            return ObjectCollectionFactory.mapFromOptional(
                    listMatches, owm -> findMax(session, owm.getMatches()));

        } catch (OperationFailedException | FeatureCalcException e) {
            throw new CreateException(e);
        }
    }

    private Optional<ObjectMask> findMax(
            FeatureCalculatorSingle<FeatureInputSingleObject> session, ObjectCollection objects)
            throws FeatureCalcException {
        Optional<ObjectMask> max = Optional.empty();
        double maxVal = 0;

        for (ObjectMask objectMask : objects) {

            double featureVal = session.calc(new FeatureInputSingleObject(objectMask));

            if (!max.isPresent() || featureVal > maxVal) {
                max = Optional.of(objectMask);
                maxVal = featureVal;
            }
        }

        return max;
    }
}
