/*-
 * #%L
 * anchor-plugin-mpp-sgmn
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

package org.anchoranalysis.plugin.mpp.segment.bean.marks;

import org.anchoranalysis.core.exception.InitException;
import org.anchoranalysis.core.exception.OperationFailedException;
import org.anchoranalysis.core.identifier.provider.NamedProviderGetException;
import org.anchoranalysis.core.log.Logger;
import org.anchoranalysis.feature.energy.EnergyStack;
import org.anchoranalysis.mpp.bean.init.MPPInitParams;
import org.anchoranalysis.mpp.feature.mark.MemoList;
import org.anchoranalysis.mpp.feature.mark.UpdatableMarksList;
import org.anchoranalysis.mpp.mark.Mark;
import org.anchoranalysis.mpp.pair.IdentifiablePair;
import org.anchoranalysis.mpp.pair.RandomCollection;

class UpdateMarkSet {

    private MPPInitParams psoImage;
    private EnergyStack energyStack;
    private UpdatableMarksList updatableMarkSetCollection;
    private Logger logger;

    public UpdateMarkSet(
            MPPInitParams psoImage,
            EnergyStack energyStack,
            UpdatableMarksList updatableMarkSetCollection,
            Logger logger) {
        super();
        this.psoImage = psoImage;
        this.energyStack = energyStack;
        this.updatableMarkSetCollection = updatableMarkSetCollection;
        this.logger = logger;
    }

    public void apply() throws OperationFailedException {
        makePairsUpdatable();
    }

    private void makePairsUpdatable() throws OperationFailedException {

        try {
            for (String key : psoImage.getSimplePairCollection().keys()) {
                RandomCollection<IdentifiablePair<Mark>> pair =
                        psoImage.getSimplePairCollection().getException(key);
                pair.initUpdatableMarks(
                        new MemoList(),
                        energyStack,
                        logger,
                        psoImage.getFeature().getSharedFeatureSet());
                updatableMarkSetCollection.add(pair);
            }
        } catch (InitException e) {
            throw new OperationFailedException(e);
        } catch (NamedProviderGetException e) {
            throw new OperationFailedException(e.summarize());
        }
    }
}
