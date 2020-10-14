/*-
 * #%L
 * anchor-plugin-quick
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

package org.anchoranalysis.plugin.quick.bean.input.filepathappend;

import java.util.List;
import org.anchoranalysis.bean.NamedBean;
import org.anchoranalysis.bean.error.BeanMisconfiguredException;
import org.anchoranalysis.io.input.bean.path.DerivePath;
import org.anchoranalysis.mpp.io.bean.input.MultiInputManager;

public abstract class FilePathBaseAppendToManager extends FilePathAppendBase {

    /**
     * @param inputManager
     * @param rootName if non-empty (and non-null) a rooted derivePath is created instead of
     *     a non rooted
     * @param regex a regular-expression that returns two groups, the first is the dataset name, the
     *     second is the file-name
     * @throws BeanMisconfiguredException
     */
    public void addToManager(MultiInputManager inputManager, String rootName, String regex)
            throws BeanMisconfiguredException {
        NamedBean<DerivePath> nb = createPathDeriver(rootName, regex);
        getListFromManager(inputManager).add(nb);
    }

    protected abstract List<NamedBean<DerivePath>> getListFromManager(
            MultiInputManager inputManager) throws BeanMisconfiguredException;
}
