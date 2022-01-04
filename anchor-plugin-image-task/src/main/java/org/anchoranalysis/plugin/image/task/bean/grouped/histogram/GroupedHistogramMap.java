/*-
 * #%L
 * anchor-plugin-image-task
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

package org.anchoranalysis.plugin.image.task.bean.grouped.histogram;

import java.io.IOException;
import org.anchoranalysis.core.exception.OperationFailedException;
import org.anchoranalysis.io.output.outputter.InputOutputContext;
import org.anchoranalysis.math.histogram.Histogram;
import org.anchoranalysis.plugin.image.task.grouped.ConsistentChannelChecker;
import org.anchoranalysis.plugin.image.task.grouped.GroupMapByName;

class GroupedHistogramMap extends GroupMapByName<Histogram, Histogram> {

    private final GroupedHistogramWriter writer;

    public GroupedHistogramMap(GroupedHistogramWriter writer, int maxValue) {
        super("histogram", () -> new Histogram(maxValue));
        this.writer = writer;
    }

    @Override
    protected void addTo(Histogram ind, Histogram agg) throws OperationFailedException {
        agg.addHistogram(ind);
    }

    @Override
    protected void writeGroupOutputInSubdirectory(
            String partName,
            Histogram agg,
            ConsistentChannelChecker channelChecker,
            InputOutputContext context)
            throws IOException {
        writer.writeHistogramToFile(agg, partName, context);
    }
}
