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

package org.anchoranalysis.anchor.plugin.quick.bean.structure;

import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.BeanInstanceMap;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.error.BeanMisconfiguredException;
import org.anchoranalysis.bean.shared.regex.RegEx;
import org.anchoranalysis.io.bean.filepath.prefixer.FilePathPrefixer;
import org.anchoranalysis.io.bean.filepath.prefixer.PathWithDescription;
import org.anchoranalysis.io.error.FilePathPrefixerException;
import org.anchoranalysis.io.filepath.prefixer.FilePathPrefix;
import org.anchoranalysis.io.filepath.prefixer.FilePathPrefixerParams;
import org.anchoranalysis.plugin.io.bean.filepath.prefixer.FilePathPrefixerAvoidResolve;
import org.anchoranalysis.plugin.io.bean.filepath.prefixer.PathRegEx;
import org.anchoranalysis.plugin.io.bean.filepath.prefixer.Rooted;

/**
 * A file path prefixer that combines a prefix with an experimentType
 *
 * <p>A convenience method for commonly used prefixer settings when the output occurs in an
 * experiment/$1/ file-system structure where $1 is the experimentType
 */
public class FilePathPrefixerExperimentStructure extends FilePathPrefixer {

    // START BEAN PROPERTIES
    @BeanField @Getter @Setter private String experimentType;

    @BeanField @Getter @Setter private String rootName;

    @BeanField @Getter @Setter private RegEx regEx;

    @BeanField @Getter @Setter private String prefix;
    // END BEAN PROPERTIES

    private FilePathPrefixer delegate;

    private BeanInstanceMap defaultInstances;

    @Override
    public void checkMisconfigured(BeanInstanceMap defaultInstances)
            throws BeanMisconfiguredException {
        super.checkMisconfigured(defaultInstances);
        this.defaultInstances = defaultInstances;
    }

    @Override
    public FilePathPrefix outFilePrefix(
            PathWithDescription input, String experimentIdentifier, FilePathPrefixerParams context)
            throws FilePathPrefixerException {

        createDelegateIfNeeded();

        return delegate.outFilePrefix(input, experimentIdentifier, context);
    }

    @Override
    public FilePathPrefix rootFolderPrefix(
            String experimentIdentifier, FilePathPrefixerParams context)
            throws FilePathPrefixerException {

        createDelegateIfNeeded();

        return delegate.rootFolderPrefix(experimentIdentifier, context);
    }

    private void createDelegateIfNeeded() throws FilePathPrefixerException {

        if (delegate != null) {
            // Nothing to do
            return;
        }

        this.delegate = wrapWithRoot(createResolver());

        try {
            this.delegate.checkMisconfigured(defaultInstances);
        } catch (BeanMisconfiguredException e) {
            throw new FilePathPrefixerException(e);
        }
    }

    private FilePathPrefixerAvoidResolve createResolver() {
        PathRegEx resolver = new PathRegEx();
        resolver.setOutPathPrefix(prefix + experimentType);
        resolver.setRegEx(regEx);
        return resolver;
    }

    private Rooted wrapWithRoot(FilePathPrefixerAvoidResolve in) {
        Rooted out = new Rooted();
        out.setRootName(rootName);
        out.setFilePathPrefixer(in);
        return out;
    }
}
