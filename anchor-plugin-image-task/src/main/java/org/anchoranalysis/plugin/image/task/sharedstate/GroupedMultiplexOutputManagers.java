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

package org.anchoranalysis.plugin.image.task.sharedstate;

import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import org.anchoranalysis.io.manifest.ManifestFolderDescription;
import org.anchoranalysis.io.manifest.sequencetype.SetSequenceType;
import org.anchoranalysis.io.output.bound.BoundOutputManagerRouteErrors;

/** A set of output-managers, one for each group */
class GroupedMultiplexOutputManagers {

    private static final ManifestFolderDescription MANIFEST_DESCRIPTION =
            new ManifestFolderDescription("groupOutput", "outputManager", new SetSequenceType());

    private Map<String, BoundOutputManagerRouteErrors> map;

    public GroupedMultiplexOutputManagers(
            BoundOutputManagerRouteErrors baseOutputManager, Set<String> groups) {

        map = new TreeMap<>();

        for (String key : groups) {
            map.put(key, baseOutputManager.deriveSubdirectory(key, MANIFEST_DESCRIPTION));
        }
    }

    // Returns null if no object exists
    public BoundOutputManagerRouteErrors getOutputManagerFor(String key) {
        return map.get(key);
    }
}
