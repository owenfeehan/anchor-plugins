package org.anchoranalysis.plugin.io.bean.descriptivename;

/*-
 * #%L
 * anchor-plugin-io
 * %%
 * Copyright (C) 2010 - 2020 Owen Feehan, ETH Zurich, University of Zurich, Hoffmann la Roche
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

import static org.junit.Assert.*;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.anchoranalysis.io.error.AnchorIOException;
import org.anchoranalysis.io.input.descriptivename.DescriptiveFile;
import org.junit.Test;

public class PatternSpanTest {

	@Test
	public void testSimple() throws AnchorIOException {
		PatternSpan ps = new PatternSpan();
		List<DescriptiveFile> ret = ps.descriptiveNamesFor( createFiles(), "<UNKNOWN>");
		
		assertIndexEquals( ret, 0, "b");
		assertIndexEquals( ret, 1, "d");
		assertIndexEquals( ret, 2, "e");
	}
	
	@Test
	public void testPaths() throws AnchorIOException {
		List<String> strs = Arrays.asList(
			"D:/Users/owen/Pictures/To Integrate/Feb 2020/P1210940.JPG",
			"D:/Users/owen/Pictures/To Integrate/Feb 2020/Klosters (Feb 2020)/P1210904.JPG"
		);
		List<File> files = strs.stream().map( str -> new File(str) ).collect( Collectors.toList() );
		
		PatternSpan ps = new PatternSpan();
		List<DescriptiveFile> ret = ps.descriptiveNamesFor(files, "unknown");
		assertIndexEquals( ret, 0, "P1210940");
		assertIndexEquals( ret, 1, "Klosters-(Feb-2020)" + File.separator + "P1210904");
	}
	
	private List<File> createFiles() {
		File file1 = new File("/a/b/c");
		File file2 = new File("/a/d/c");
		File file3 = new File("/a/e/c");
		return Arrays.asList(file1, file2, file3);
	}
	
	private void assertIndexEquals( List<DescriptiveFile> ret, int index, String expected ) {
		String actual = ret.get(index).getDescriptiveName();
		assertEquals( expected, actual );
	}
}
