/*-
 * #%L
 * anchor-plugin-io
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

package org.anchoranalysis.plugin.io.manifest;

import java.nio.file.Path;
import java.util.Optional;
import lombok.Getter;
import org.anchoranalysis.core.log.Logger;
import org.anchoranalysis.core.path.PathDifference;
import org.anchoranalysis.core.path.PathDifferenceException;
import org.anchoranalysis.io.input.InputFromManager;
import org.anchoranalysis.io.manifest.Manifest;
import org.anchoranalysis.plugin.io.bean.descriptivename.LastDirectories;

// A file manifest together with the overall manifest for the experiment
public class CoupledManifests implements InputFromManager {

    @Getter private final Optional<Manifest> experimentManifest;

    @Getter private final DeserializedManifest jobManifest;

    private final String name;

    public CoupledManifests(
            DeserializedManifest jobManifest, int numberDirectoriesInDescription, Logger logger) {
        super();
        this.experimentManifest = Optional.empty();
        this.jobManifest = jobManifest;
        name = generateNameFromDirectories(numberDirectoriesInDescription, logger);
    }

    public CoupledManifests(
            Manifest experimentManifest, DeserializedManifest jobManifest, Logger logger)
            throws PathDifferenceException {
        super();
        this.experimentManifest = Optional.of(experimentManifest);
        this.jobManifest = jobManifest;
        name = generateName(logger);
    }


    private String generateName(Logger logger) throws PathDifferenceException {

        if (experimentManifest.isPresent()) {
            Path experimentRootFolder =
                    getExperimentManifest().get().getRootFolder().calculatePath();

            PathDifference ff =
                    PathDifference.differenceFrom(experimentRootFolder, jobManifest.getRootPath());
            return ff.combined().toString();

        } else {
            return generateNameFromDirectories(0, logger);
        }
    }

    private String generateNameFromDirectories(int numberDirectoriesInDescription, Logger logger) {
        LastDirectories dnff = new LastDirectories(numberDirectoriesInDescription);
        dnff.setRemoveExtensionInDescription(false);
        return dnff.deriveName(jobManifest.getRootPath().toFile(), "<unknown>", logger)
                .getName();
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public Optional<Path> pathForBinding() {
        return Optional.of(jobManifest.getRootPath());
    }
}
