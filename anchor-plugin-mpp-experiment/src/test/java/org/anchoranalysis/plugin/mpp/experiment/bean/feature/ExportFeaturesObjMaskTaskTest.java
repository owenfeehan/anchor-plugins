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

import java.util.Arrays;
import java.util.List;

import org.anchoranalysis.bean.NamedBean;
import org.anchoranalysis.bean.xml.RegisterBeanFactories;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.feature.bean.list.FeatureListProvider;
import org.anchoranalysis.feature.calc.params.FeatureCalcParams;
import org.anchoranalysis.feature.nrg.NRGStack;
import org.anchoranalysis.image.bean.provider.ObjMaskProvider;
import org.anchoranalysis.image.bean.provider.stack.StackProvider;
import org.anchoranalysis.image.feature.objmask.FeatureObjMaskParams;
import org.anchoranalysis.image.stack.Stack;
import org.anchoranalysis.plugin.mpp.experiment.bean.feature.flexi.FlexiFeatureTable;
import org.anchoranalysis.plugin.mpp.experiment.bean.feature.flexi.MergedPairs;
import org.anchoranalysis.plugin.mpp.experiment.bean.feature.flexi.Simple;
import org.anchoranalysis.test.TestLoader;
import org.anchoranalysis.test.feature.plugins.FeaturesFromXmlFixture;
import org.anchoranalysis.test.feature.plugins.NRGStackFixture;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import static org.mockito.Mockito.*;

import ch.ethz.biol.cell.imageprocessing.objmask.provider.ObjMaskProviderReference;


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
	
	static {
		RegisterBeanFactories.registerAllPackageBeanFactories();
	}
	
	private static TestLoader loader = TestLoader.createFromMavenWorkingDir();
	
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

	@Test(expected=OperationFailedException.class)
	public void testSimpleSmall() throws OperationFailedException, CreateException {
		// The saved directory is irrelevant because an exception is thrown
		testOnTask( new Simple(), false, "irrelevant" );
	}
	
	@Test
	public void testSimpleLarge() throws OperationFailedException, CreateException {
		testOnTask( new Simple(), true, "simple01/" );
	}
	
	@Test(expected=OperationFailedException.class)
	public void testMergedSmall() throws OperationFailedException, CreateException {
		// The saved directory is irrelevant because an exception is thrown
		testOnTask( new MergedPairs(), false, "irrelevant" );
	}
	
	@Test
	public void testMergedLarge() throws OperationFailedException, CreateException {
		testOnTask( new MergedPairs(), true, "mergedPairs01/" );
	}
	
	private <T extends FeatureCalcParams> void testOnTask(
		FlexiFeatureTable<T> selectFeaturesObjects,
		boolean bigSizeNrg,
		String suffixPathDirSaved
	) throws OperationFailedException, CreateException {
		
		NRGStack nrgStack = NRGStackFixture.create(bigSizeNrg, false).getNrgStack();
		
		TaskSingleInputHelper.runTaskAndCompareOutputs(
			MultiInputFixture.createInput(nrgStack),
			createTask( selectFeaturesObjects, nrgStack ),
			folder.getRoot().toPath(),
			RELATIVE_PATH_SAVED_RESULTS + suffixPathDirSaved,
			OUTPUTS_TO_COMPARE
		);	
	}
	
	private static <T extends FeatureCalcParams> ExportFeaturesObjMaskTask<T> createTask(
		FlexiFeatureTable<T> selectFeaturesObjects,
		NRGStack nrgStack
	) throws CreateException {
		ExportFeaturesObjMaskTask<T> task = new ExportFeaturesObjMaskTask<>();
		task.setListFeaturesObjMask(
			objMaskFeatures(loader)
		);
		task.setNrgStackProvider(
			nrgStackProvider(nrgStack)
		);
		task.setSelectFeaturesObjects(
			selectFeaturesObjects
		);
		task.setListObjMaskProvider(
			createObjProviders(MultiInputFixture.OBJS_NAME)
		);
		return task;
	}
	
	private static List<NamedBean<ObjMaskProvider>> createObjProviders(String objsName) {
		return Arrays.asList(
			new NamedBean<>(objsName, new ObjMaskProviderReference(objsName))	
		);
	}

	private static StackProvider nrgStackProvider(NRGStack nrgStack) throws CreateException {

		// Create NRG stack 
		Stack stack = nrgStack.asStack();
		
		// Encapsulate in a mock
		StackProvider stackProvider = mock(StackProvider.class);
		when(stackProvider.create()).thenReturn(stack);
		when(stackProvider.duplicateBean()).thenReturn(stackProvider);
		return stackProvider;
	}
	
	/** creates a feature-list associated with obj-mask
	 *  
	 * @throws CreateException 
	 * */
	private static List<NamedBean<FeatureListProvider<FeatureObjMaskParams>>> objMaskFeatures( TestLoader loader ) throws CreateException {
		return FeaturesFromXmlFixture.createNamedFeatureProviders("namedObjMaskFeaturesList.xml", loader);
	}
}
