package org.anchoranalysis.plugin.mpp.experiment.bean.feature;

/*
 * #%L
 * anchor-plugin-mpp-experiment
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


import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.anchoranalysis.bean.NamedBean;
import org.anchoranalysis.bean.annotation.AllowEmpty;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.error.InitException;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.core.index.GetOperationFailedException;
import org.anchoranalysis.core.log.LogErrorReporter;
import org.anchoranalysis.core.name.CombinedName;
import org.anchoranalysis.core.name.MultiName;
import org.anchoranalysis.experiment.ExperimentExecutionException;
import org.anchoranalysis.experiment.JobExecutionException;
import org.anchoranalysis.experiment.task.InputTypesExpected;
import org.anchoranalysis.experiment.task.InputBound;
import org.anchoranalysis.experiment.task.ParametersExperiment;
import org.anchoranalysis.feature.bean.list.FeatureListProvider;
import org.anchoranalysis.feature.calc.results.ResultsVectorCollection;
import org.anchoranalysis.feature.input.FeatureInput;
import org.anchoranalysis.feature.io.csv.GroupedResultsVectorCollection;
import org.anchoranalysis.feature.nrg.NRGStackWithParams;
import org.anchoranalysis.image.bean.provider.ObjMaskProvider;
import org.anchoranalysis.image.feature.objmask.FeatureInputSingleObj;
import org.anchoranalysis.image.feature.session.FeatureTableSession;
import org.anchoranalysis.image.init.ImageInitParams;
import org.anchoranalysis.image.objmask.ObjMaskCollection;
import org.anchoranalysis.io.error.AnchorIOException;
import org.anchoranalysis.io.output.bound.BoundIOContext;
import org.anchoranalysis.io.output.bound.BoundOutputManagerRouteErrors;
import org.anchoranalysis.mpp.io.input.MultiInput;
import org.anchoranalysis.mpp.sgmn.bean.define.DefineOutputterMPPWithNrg;
import org.anchoranalysis.plugin.image.feature.bean.obj.table.FeatureTableObjs;
import org.anchoranalysis.plugin.image.task.bean.feature.ExportFeaturesTask;
import org.anchoranalysis.plugin.image.task.sharedstate.SharedStateExportFeatures;


/** Calculates feature on a 'grouped' set of objects
 
   1. All files are aggregated into groups (with the name of the ObjMaskProvider added to the end)
   2. For each image file, the NamedDefinitions are applied and an ObjMaskCollection is extracted
   3. These objects are added to the appropriate ObjMaskCollection associated with each group
   4. ReportFeatures are calculated on each group, and exported as a properties file
   
   csvAll  		one csv file where each row is an object
   csvAgg  		one csv file where each row is a group (with aggregated features of the objects within)
   csvGroup 	a csv file per group, where each row is an object
   
   @param T the feature input-type supported by the FlexiFeatureTable
   
   TODO does this need to be a MultiInput and dependent on MPP? Can it be moved to anchor-plugin-image-task??
   
**/
public class ExportFeaturesObjMaskTask<T extends FeatureInput> extends ExportFeaturesTask<MultiInput,SharedStateExportFeaturesObjMask<T>> {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5961126655531145104L;
	
	// START BEAN PROPERTIES
	@BeanField
	private DefineOutputterMPPWithNrg define = new DefineOutputterMPPWithNrg();
	
	@BeanField
	private List<NamedBean<FeatureListProvider<FeatureInputSingleObj>>> listFeaturesObjMask = new ArrayList<>();
	
	@BeanField
	private List<NamedBean<ObjMaskProvider>> listObjMaskProvider = new ArrayList<>();
	
	@BeanField
	private FeatureTableObjs<T> selectFeaturesObjects;
	
	/**
	 * If non-empty, A keyValueParams is treated as part of the nrgStack 
	 */
	@BeanField @AllowEmpty
	private String nrgParamsName = "";
	
	// START BEAN PROPERTIES
	@BeanField
	private boolean suppressErrors = false;
	//END BEAN PROPERTIES

	
	@Override
	public SharedStateExportFeaturesObjMask<T> beforeAnyJobIsExecuted(
			BoundOutputManagerRouteErrors outputManager,
			ParametersExperiment params
	) throws ExperimentExecutionException {
		
		try {
			return new SharedStateExportFeaturesObjMask<>(
				new GroupedResultsVectorCollection("id","group","objSetName"),
				selectFeaturesObjects.createFeatures(listFeaturesObjMask, suppressErrors)
			);
			
		} catch (CreateException | InitException e) {
			throw new ExperimentExecutionException(e);
		}
	}
	
	@Override
	public InputTypesExpected inputTypesExpected() {
		return new InputTypesExpected(MultiInput.class);
	}

	@Override
	public void doJobOnInputObject(	InputBound<MultiInput,SharedStateExportFeaturesObjMask<T>> input ) throws JobExecutionException {
		
		try {
			define.processInput(
				input.getInputObject(),
				input.context(),
				(initParams, nrgStack) -> calculateFeaturesForImage(input, initParams, nrgStack) 
			);
						
		} catch (OperationFailedException e) {
			throw new JobExecutionException(e);
		}
	}
	
	private int calculateFeaturesForImage(
		InputBound<MultiInput,SharedStateExportFeaturesObjMask<T>> input,
		ImageInitParams imageInit,
		NRGStackWithParams nrgStack
	) throws OperationFailedException {
		
		BoundIOContext context = input.context();
		
		// Create a duplicated featureStore for this image, as we want separate features on this thread,
		//  so they are thread-safe, for parallel execution
		FeatureTableSession<T> session = input.getSharedState().getSession().duplicateForNewThread();
		
		try {
			session.start(
				imageInit,
				Optional.of(nrgStack),
				context.getLogger()
			);
		} catch (InitException e) {
			throw new OperationFailedException(e);
		}
					
		processAllProviders(
			input.getInputObject().pathForBinding(),
			session,
			imageInit,
			nrgStack,
			input.getSharedState(),
			context
		);
		
		// Arbitrary, we need a return-type
		return 0;
	}

	
	private void processAllProviders(
		Path inputPath,
		FeatureTableSession<T> session,
		ImageInitParams imageInitParams,
		NRGStackWithParams nrgStack,
		SharedStateExportFeatures sharedState,
		BoundIOContext context		
	) throws OperationFailedException {
		try {
			String id = extractImageIdentifier(inputPath, context.isDebugEnabled());
			
			// Extract a group name
			String groupName = extractGroupName(inputPath, context.isDebugEnabled());
									
			// For every objMaskCollection provider
			for( NamedBean<ObjMaskProvider> ni : listObjMaskProvider) {
				MultiName rowName = new CombinedName( groupName, ni.getName() ); 
				calculateFeaturesForProvider(
					id,
					objsFromProvider(ni.getValue(), imageInitParams, context.getLogger()),
					session,
					nrgStack,
					sharedState.resultsVectorForIdentifier(rowName),
					context.getLogger()
				);
			}
		} catch (AnchorIOException | GetOperationFailedException e) {
			throw new OperationFailedException(e);
		}
	}
	
	
	private void calculateFeaturesForProvider(
		String id,
		ObjMaskCollection objs,
		FeatureTableSession<T> session,
		NRGStackWithParams nrgStack,
		ResultsVectorCollection resultsDestination,
		LogErrorReporter logErrorReporter
	) throws OperationFailedException {
		try {
			List<T> listParams = selectFeaturesObjects.createListInputs(
				objs,
				nrgStack,
				logErrorReporter
			);
			
			FeatureCalculator.calculateManyFeaturesInto(
				id,
				session,
				listParams,
				resultsDestination,
				suppressErrors,
				logErrorReporter
			);
		} catch (CreateException | OperationFailedException e) {
			throw new OperationFailedException(e);
		}
	}
	
	private static ObjMaskCollection objsFromProvider( ObjMaskProvider provider, ImageInitParams imageInitParams, LogErrorReporter logErrorReporter ) throws OperationFailedException {

		try {
			ObjMaskProvider objMaskProviderLoc = provider.duplicateBean();
			
			// Initialise
			objMaskProviderLoc.initRecursive(imageInitParams, logErrorReporter);
	
			return objMaskProviderLoc.create(); 
			
		} catch (InitException | CreateException e) {
			throw new OperationFailedException(e);
		}
	}

	public List<NamedBean<FeatureListProvider<FeatureInputSingleObj>>> getListFeaturesObjMask() {
		return listFeaturesObjMask;
	}

	public void setListFeaturesObjMask(
			List<NamedBean<FeatureListProvider<FeatureInputSingleObj>>> listFeaturesObjMask) {
		this.listFeaturesObjMask = listFeaturesObjMask;
	}

	public List<NamedBean<ObjMaskProvider>> getListObjMaskProvider() {
		return listObjMaskProvider;
	}

	public void setListObjMaskProvider(
			List<NamedBean<ObjMaskProvider>> listObjMaskProvider) {
		this.listObjMaskProvider = listObjMaskProvider;
	}


	public FeatureTableObjs<T> getSelectFeaturesObjects() {
		return selectFeaturesObjects;
	}


	public void setSelectFeaturesObjects(FeatureTableObjs<T> selectFeaturesObjects) {
		this.selectFeaturesObjects = selectFeaturesObjects;
	}

	public String getNrgParamsName() {
		return nrgParamsName;
	}

	public void setNrgParamsName(String nrgParamsName) {
		this.nrgParamsName = nrgParamsName;
	}

	public boolean isSuppressErrors() {
		return suppressErrors;
	}

	public void setSuppressErrors(boolean suppressErrors) {
		this.suppressErrors = suppressErrors;
	}

	public DefineOutputterMPPWithNrg getDefine() {
		return define;
	}

	public void setDefine(DefineOutputterMPPWithNrg define) {
		this.define = define;
	}
}
