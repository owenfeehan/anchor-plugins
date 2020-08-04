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

package ch.ethz.biol.cell.mpp.feedback.reporter;

import java.util.Optional;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.anchoranalysis.anchor.mpp.feature.nrg.cfg.CfgNRGPixelized;
import org.anchoranalysis.io.manifest.ManifestDescription;
import org.anchoranalysis.io.output.file.FileOutput;
import org.anchoranalysis.io.output.file.FileOutputFromManager;
import org.anchoranalysis.mpp.sgmn.optscheme.feedback.OptimizationFeedbackInitParams;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CSVReporterUtilities {

    public static Optional<FileOutput> createFileOutputFor(
            String outputName,
            OptimizationFeedbackInitParams<CfgNRGPixelized> initParams,
            String manifestDscrFunction)
            {
        return FileOutputFromManager.create(
                "csv",
                Optional.of(new ManifestDescription("csv", manifestDscrFunction)),
                initParams.getInitContext().getOutputManager().getDelegate(),
                outputName);
    }
}
