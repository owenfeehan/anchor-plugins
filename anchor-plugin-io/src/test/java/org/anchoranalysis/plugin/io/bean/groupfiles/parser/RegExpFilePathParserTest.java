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

package org.anchoranalysis.plugin.io.bean.groupfiles.parser;

import static org.junit.Assert.*;

import org.junit.Test;

public class RegExpFilePathParserTest {

    @Test
    public void testSetPath() {

        RegExpFilePathParser parser = new RegExpFilePathParser();

        // Always do expression first
        parser.setZSliceGroupID(1);
        parser.setChnlGroupID(2);
        parser.setExpression(".*hello_(\\d*)_(\\d*)_.*");

        parser.setPath("hello_4_2_world");

        assertTrue(parser.getChnlNum().get() == 2);
        assertTrue(parser.getZSliceNum().get() == 4);

        parser.setChnlGroupID(0);

        parser.setPath("hello_5_7_world");

        assertTrue(parser.getZSliceNum().get() == 5);
    }
}
