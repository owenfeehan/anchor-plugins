package ch.ethz.biol.cell.imageprocessing.io.generator.text;

/*
 * #%L
 * anchor-plugin-mpp
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


import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Path;

import org.anchoranalysis.io.generator.Generator;
import org.anchoranalysis.io.generator.IterableGenerator;
import org.anchoranalysis.io.generator.SingleFileTypeGenerator;
import org.anchoranalysis.io.manifest.ManifestDescription;
import org.anchoranalysis.io.output.bean.OutputWriteSettings;
import org.anchoranalysis.io.output.error.OutputWriteFailedException;

public class ObjectAsStringGenerator<T> extends SingleFileTypeGenerator implements IterableGenerator<T> {

	private T object = null;
		
	public ObjectAsStringGenerator() {
		super();
	}
	
	public ObjectAsStringGenerator(T object) {
		super();
		this.object = object;
	}

	@Override
	public void writeToFile(OutputWriteSettings outputWriteSettings,
			Path filePath) throws OutputWriteFailedException {
		
		if (getIterableElement()==null) {
			throw new OutputWriteFailedException("no mutable element set");
		}
		
		try (FileWriter outFile = new FileWriter( filePath.toFile() )) {
			
			PrintWriter out = new PrintWriter(outFile);
			out.println( object.toString() );
			
		} catch (IOException e) {
			throw new OutputWriteFailedException(e);
		}
	}

	@Override
	public String getFileExtension(OutputWriteSettings outputWriteSettings) {
		return outputWriteSettings.getExtensionText();
	}

	@Override
	public T getIterableElement() {
		return this.object;
	}

	@Override
	public void setIterableElement(T element) {
		this.object = element;
	}

	@Override
	public Generator getGenerator() {
		return this;
	}

	
	@Override
	public ManifestDescription createManifestDescription() {
		return new ManifestDescription("text", "object");
	}
	
	@Override
	public void start() throws OutputWriteFailedException {
	}


	@Override
	public void end() throws OutputWriteFailedException {
	}

}
