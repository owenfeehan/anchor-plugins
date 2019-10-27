package ch.ethz.biol.cell.sgmn.objmask.watershed.encoding;

/*
 * #%L
 * anchor-plugin-image
 * %%
 * Copyright (C) 2016 ETH Zurich, University of Zurich, Owen Feehan
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


// Maps directions to chain codes
public class ChainCodesDirection {

	// chain code 0
	public static final int MAX_VALUE = 27;

	public ChainCodesDirection() {
		
	}
	
	// x, y, z  are -1, 0 or 1, for 3^3 combinations
	public int chainCode( int x, int y, int z ) {
		return ((z+1) * 9) + ((y+1) * 3) + (x+1);
	}

	public int xFromChainCode( int chainCode ) {
		return (chainCode % 3) -1;
	}
	
	public int yFromChainCode( int chainCode ) {
		return ((chainCode % 9) / 3) -1;
	}
	
	public int zFromChainCode( int chainCode ) {
		return (chainCode / 9) - 1;
	}
	
	
}
