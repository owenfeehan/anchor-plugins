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

package org.anchoranalysis.plugin.io.shared;

import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import org.anchoranalysis.core.value.LanguageUtilities;

/**
 * Counts the frequency of certain strings, and describes the contents in human language.
 *
 * @author Owen Feehan
 * @param <T> key-type (should have .toString() representation that is meaningful to humans)
 */
public class FrequencyMap<T> {

    private Map<T, Integer> map = new TreeMap<>();

    public synchronized void incrementCount(T key) {

        Integer count = map.get(key);

        if (count == null) {
            map.put(key, 1);
        } else {
            map.put(key, count + 1);
        }
    }

    public synchronized String describe(String dscrNoun) {
        int numKeys = map.keySet().size();

        if (numKeys == 0) {
            return "No inputs have been found yet.";
        } else if (numKeys == 1) {
            return String.format("with uniform %s = %s", dscrNoun, map.keySet().iterator().next());
        } else {
            return describeDiverse(dscrNoun);
        }
    }

    private String describeDiverse(String dscrNoun) {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("with diverse %s:", LanguageUtilities.pluralize(dscrNoun)));

        for (Entry<T, Integer> entry : map.entrySet()) {
            sb.append(String.format(" %s(%d)", entry.getKey(), entry.getValue()));
        }

        return sb.toString();
    }
}
