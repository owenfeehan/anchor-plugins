package org.anchoranalysis.plugin.mpp.experiment.bean.feature;

/*-
 * #%L
 * anchor-plugin-mpp-experiment
 * %%
 * Copyright (C) 2010 - 2020 Owen Feehan
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

import org.anchoranalysis.bean.xml.RegisterBeanFactories;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.test.TestLoader;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;


/**
 * Tests running {#link ExportFeaturesObjMaskTask} on a single input
 * 
 * <p>Two types of NRG stack are supported: big and small</p>
 * <p>For the small NRG stack, some of the object-masks are outside the scene size</p>
 * 
 * @author owen
 *
 */
public class ExportFeaturesObjMaskTaskTest {
	
	private static TestLoader loader;
	private ExportFeaturesObjMaskTaskFixture taskFixture;
		
	private static final String RELATIVE_PATH_SAVED_RESULTS = "expectedOutput/exportFeaturesObjMask/";
	
	private static final String[] OUTPUTS_TO_COMPARE = {
		"csvAgg.csv",
		"csvAll.csv",
		"arbitraryPath/objsTest/csvGroup.csv",
		"stackCollection/input.tif",
		"nrgStack/nrgStack_00.tif",
		"manifest.ser.xml",
		"nrgStackParams.xml",
		"arbitraryPath/objsTest/paramsGroupAgg.xml",
		"objMaskCollection/objsTest.h5"
	};
		
	@Rule
	public TemporaryFolder folder = new TemporaryFolder();

	@BeforeClass
	public static void setup() {
		RegisterBeanFactories.registerAllPackageBeanFactories();
		loader = TestLoader.createFromMavenWorkingDir();
	}
	
	@Before
	public void setupTest() throws CreateException {
		taskFixture = new ExportFeaturesObjMaskTaskFixture(loader);
	}
	
	@Test(expected=OperationFailedException.class)
	public void testSimpleSmall() throws OperationFailedException, CreateException {
		// The saved directory is irrelevant because an exception is thrown
		taskFixture.useSmallNRGInstead();
		testOnTask("irrelevant");
	}
	
	@Test
	public void testSimpleLarge() throws OperationFailedException, CreateException {
		testOnTask("simple01/");
	}
	
	@Test(expected=OperationFailedException.class)
	public void testMergedSmall() throws OperationFailedException, CreateException {
		// The saved directory is irrelevant because an exception is thrown
		taskFixture.useSmallNRGInstead();
		taskFixture.changeToMergedPairs(false);
		testOnTask("irrelevant");
	}
	
	@Test
	public void testMergedLarge() throws OperationFailedException, CreateException {
		taskFixture.changeToMergedPairs(false);
		testOnTask("mergedPairs01/");
	}
	
	@Test
	public void testMergedLargeWithPairs() throws OperationFailedException, CreateException {
		taskFixture.includeAdditionalShellFeature();
		taskFixture.changeToMergedPairs(true);
		testOnTask("mergedPairs02/");
	}
	
	/**
	 *  Tests when a particular FeatureCalculation is called by a feature in both the Single and Pair part of merged-pairs.
	 *  
	 *  <p>To be efficient, this calculation should only occur once, as the caches should be linked</p>.
	 * 
	 * @throws OperationFailedException
	 * @throws CreateException
	 */
	@Test
	public void testRepeatedCalculationInSingleAndPair() throws OperationFailedException, CreateException {
		taskFixture.includeAdditionalShellFeature();
		taskFixture.changeToMergedPairs(true);
		testOnTask("mergedPairs02/");
	}
	
	/**
	 * Runs a test to check if the results of ExportFeaturesObjMaskTask correspond to saved-values
	 *
	 * @param suffixPathDirSaved a suffix to identify where to find the saved-output to compare against
	 * @throws OperationFailedException
	 * @throws CreateException
	 */
	private void testOnTask(String suffixPathDirSaved) throws OperationFailedException, CreateException {
		
		TaskSingleInputHelper.runTaskAndCompareOutputs(
			MultiInputFixture.createInput( taskFixture.getNrgStack() ),
			taskFixture.createTask(),
			folder.getRoot().toPath(),
			RELATIVE_PATH_SAVED_RESULTS + suffixPathDirSaved,
			OUTPUTS_TO_COMPARE
		);	
	}
}
