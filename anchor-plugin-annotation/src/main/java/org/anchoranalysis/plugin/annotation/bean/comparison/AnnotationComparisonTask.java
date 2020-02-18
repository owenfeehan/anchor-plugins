package org.anchoranalysis.plugin.annotation.bean.comparison;

/*
 * #%L
 * anchor-annotation
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


import java.io.IOException;

import org.anchoranalysis.annotation.io.assignment.Assignment;
import org.anchoranalysis.annotation.io.assignment.AssignmentObjMask;
import org.anchoranalysis.annotation.io.assignment.generator.AssignmentGeneratorFactory;
import org.anchoranalysis.annotation.io.assignment.generator.ColorPool;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.core.index.GetOperationFailedException;
import org.anchoranalysis.core.log.LogErrorReporter;
import org.anchoranalysis.core.log.LogReporter;
import org.anchoranalysis.core.progress.ProgressReporterNull;
import org.anchoranalysis.experiment.ExperimentExecutionException;
import org.anchoranalysis.experiment.JobExecutionException;
import org.anchoranalysis.experiment.task.ParametersBound;
import org.anchoranalysis.experiment.task.ParametersExperiment;
import org.anchoranalysis.experiment.task.Task;
import org.anchoranalysis.image.io.input.StackInput;
import org.anchoranalysis.image.stack.DisplayStack;
import org.anchoranalysis.image.stack.NamedImgStackCollection;
import org.anchoranalysis.image.stack.wrap.WrapStackAsTimeSequenceStore;
import org.anchoranalysis.io.bean.color.generator.ColorSetGenerator;
import org.anchoranalysis.io.bean.color.generator.VeryBrightColorSetGenerator;
import org.anchoranalysis.io.output.bound.BoundOutputManagerRouteErrors;
import org.anchoranalysis.plugin.annotation.bean.comparison.assigner.AnnotationComparisonAssigner;
import org.anchoranalysis.plugin.annotation.comparison.AnnotationComparisonInput;
import org.anchoranalysis.plugin.annotation.comparison.IAddAnnotation;
import org.anchoranalysis.plugin.annotation.comparison.ObjsToCompare;

import ch.ethz.biol.cell.countchrom.experiment.SplitString;

public class AnnotationComparisonTask<T extends Assignment> extends Task<AnnotationComparisonInput<StackInput>,SharedState<T>> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	// START BEAN PROPERTIES
	@BeanField
	private String backgroundChnlName = "Image";
	
	// If a non-empty string it is used to split the descriptive name into groups
	@BeanField
	private String splitDescriptiveNameRegex = "";	
	
	@BeanField
	private int maxSplitGroups = 5;
	
	@BeanField
	private int numLevelsGrouping = 0;
	
	@BeanField
	private boolean useMIP = false;
	
	@BeanField
	private int outlineWidth = 1;
	
	@BeanField
	private String nameLeft = "annotation";
	
	@BeanField
	private String nameRight = "result";
	
	@BeanField
	private AnnotationComparisonAssigner<T> assigner;
	
	@BeanField
	private boolean replaceMatchesWithSolids = true;
	// END BEAN PROPERTIES
	
	private ColorSetGenerator colorSetGeneratorUnpaired = new VeryBrightColorSetGenerator();

	@Override
	public SharedState<T> beforeAnyJobIsExecuted(
			BoundOutputManagerRouteErrors outputManager, ParametersExperiment params)
			throws ExperimentExecutionException {
	
		try {
			CSVAssignment assignmentCSV = new CSVAssignment(outputManager,"byImage",hasDescriptiveSplit(),maxSplitGroups);
			return new SharedState<T>(
				assignmentCSV,
				numLevelsGrouping,
				key -> assigner.groupForKey(key)
			);
		} catch (IOException e) {
			throw new ExperimentExecutionException(e);
		}
	}
	
	@Override
	protected void doJobOnInputObject(
		ParametersBound<AnnotationComparisonInput<StackInput>,SharedState<T>> params
	) throws JobExecutionException {

		LogErrorReporter logErrorReporter = params.getLogErrorReporter();
		AnnotationComparisonInput<StackInput> input = params.getInputObject();
		
		// Create the background
		DisplayStack background = createBackground(input);

		// We only do a descriptive split if it's allowed
		SplitString descriptiveSplit = createSplitString(input);  
		
		// Now do whatever comparison is necessary to update the assignment
		Assignment assignment = compareAndUpdate(
			input,
			background,
			descriptiveSplit,
			params.getExperimentArguments().isDebugEnabled(),
			logErrorReporter,
			params.getOutputManager(),
			params.getSharedState()
		);
		
		
		if (assignment==null) {
			return;
		}

		try {
			writeRGBOutlineStack("rgbOutline", params.getOutputManager(), input, assignment, background);
		} catch (OperationFailedException e) {
			logErrorReporter.getErrorReporter().recordError(AnnotationComparisonTask.class, e);
		}
	}
	
	private Assignment compareAndUpdate(
		AnnotationComparisonInput<StackInput> input,
		DisplayStack background,
		SplitString descriptiveSplit,
		boolean debugEnabled,
		LogErrorReporter logErrorReporter,
		BoundOutputManagerRouteErrors outputManager,
		SharedState<T> sharedState
	) throws JobExecutionException {
		
		// Get a matching set of groups for this image
		IAddAnnotation<T> addAnnotation = sharedState.groupsForImage(
			input.descriptiveName(),
			descriptiveSplit
		);
		
		ObjsToCompare objs = ObjsToCompareFactory.create(
			input,
			addAnnotation,
			logErrorReporter,
			background.getDimensions(),
			debugEnabled
		);
		
		if (objs==null) {
			return null;
		}
		
		return processAcceptedAnnotation(
			input,
			background,
			objs,			
			addAnnotation,
			sharedState,
			outputManager,
			descriptiveSplit,
			logErrorReporter
		);
	}
	
	
	private SplitString createSplitString( AnnotationComparisonInput<StackInput> input ) {
		return hasDescriptiveSplit() ? new SplitString(input.descriptiveName(), splitDescriptiveNameRegex) : null;
	}
	
	private DisplayStack createBackground( AnnotationComparisonInput<StackInput> inputObject ) throws JobExecutionException {
		
		try {
			NamedImgStackCollection stackCollection = new NamedImgStackCollection();
			
			inputObject.getInputObject().addToStore(
				new WrapStackAsTimeSequenceStore( stackCollection ),
				0,
				ProgressReporterNull.get()
			);

			return DisplayStack.create( stackCollection.getException(backgroundChnlName) );
			
		} catch (CreateException | GetOperationFailedException | OperationFailedException e) {
			throw new JobExecutionException(e);
		}
	}
	
	private Assignment processAcceptedAnnotation(
		AnnotationComparisonInput<StackInput> inputObject,
		DisplayStack background,
		ObjsToCompare objs,
		IAddAnnotation<T> addAnnotation,
		SharedState<T> sharedStateC,
		BoundOutputManagerRouteErrors outputManager,
		SplitString descriptiveSplit,
		LogErrorReporter logErrorReporter
	) throws JobExecutionException {
		
		
		logErrorReporter.getLogReporter().log("Start processAcceptedAnnotation");
		
		try {
			T assignment = assigner.createAssignment(
				objs,
				background.getDimensions(),
				useMIP,
				outputManager,
				logErrorReporter
			);
			
			addAnnotation.addAcceptedAnnotation(assignment);
	
			// Statistics row in file
			sharedStateC.getAssignmentCSV().writeStatisticsForImage(
				assignment,
				descriptiveSplit,					
				inputObject
			);
				
			return assignment;
			
		} catch (CreateException e) {
			throw new JobExecutionException(e);
		}
	}
	
	
	

	private boolean hasDescriptiveSplit() {
		return splitDescriptiveNameRegex!=null && !splitDescriptiveNameRegex.isEmpty();
	}
	
	private void writeRGBOutlineStack(
		String outputName,
		BoundOutputManagerRouteErrors outputManager,
		AnnotationComparisonInput<StackInput> inputObject,
		Assignment assignment,
		DisplayStack background
	) throws OperationFailedException {
		
		if (!outputManager.isOutputAllowed(outputName)) {
			return;
		}
		
		ColorPool colorPool = new ColorPool(
			assignment.numPaired(),
			outputManager.getOutputWriteSettings().getDefaultColorSetGenerator(),
			colorSetGeneratorUnpaired,
			replaceMatchesWithSolids
		);

		outputManager.getWriterCheckIfAllowed().write(
			"rgbOutline",
			() -> AssignmentGeneratorFactory.createAssignmentGenerator(
				background,
				assignment,
				colorPool,
				useMIP,
				inputObject.getNameLeft()!=null ? inputObject.getNameLeft() : nameLeft,
				inputObject.getNameRight()!=null ? inputObject.getNameRight() : nameRight,
				outlineWidth,
				assigner.moreThanOneObj()
			)
		);
	}

	@Override
	public void afterAllJobsAreExecuted(
			BoundOutputManagerRouteErrors outputManager, SharedState<T> sharedState, LogReporter logReporter)
			throws ExperimentExecutionException {

		@SuppressWarnings("unchecked")
		SharedState<AssignmentObjMask> sharedStateC = (SharedState<AssignmentObjMask>) sharedState;
		sharedStateC.getAssignmentCSV().end();

		// Write group statistics
		try {
			new CSVComparisonGroup<>(sharedStateC.allGroups()).writeGroupStats( outputManager );
		} catch (IOException e) {
			throw new ExperimentExecutionException(e);
		}
	}

	public String getBackgroundChnlName() {
		return backgroundChnlName;
	}

	public void setBackgroundChnlName(String backgroundChnlName) {
		this.backgroundChnlName = backgroundChnlName;
	}

	public String getSplitDescriptiveNameRegex() {
		return splitDescriptiveNameRegex;
	}

	public void setSplitDescriptiveNameRegex(String splitDescriptiveNameRegex) {
		this.splitDescriptiveNameRegex = splitDescriptiveNameRegex;
	}

	public int getMaxSplitGroups() {
		return maxSplitGroups;
	}

	public void setMaxSplitGroups(int maxSplitGroups) {
		this.maxSplitGroups = maxSplitGroups;
	}

	public int getNumLevelsGrouping() {
		return numLevelsGrouping;
	}

	public void setNumLevelsGrouping(int numLevelsGrouping) {
		this.numLevelsGrouping = numLevelsGrouping;
	}

	public boolean isUseMIP() {
		return useMIP;
	}

	public void setUseMIP(boolean useMIP) {
		this.useMIP = useMIP;
	}

	public int getOutlineWidth() {
		return outlineWidth;
	}

	public void setOutlineWidth(int outlineWidth) {
		this.outlineWidth = outlineWidth;
	}

	public AnnotationComparisonAssigner<T> getAssigner() {
		return assigner;
	}

	public void setAssigner(AnnotationComparisonAssigner<T> assigner) {
		this.assigner = assigner;
	}

	public String getNameLeft() {
		return nameLeft;
	}

	public void setNameLeft(String nameLeft) {
		this.nameLeft = nameLeft;
	}

	public String getNameRight() {
		return nameRight;
	}

	public void setNameRight(String nameRight) {
		this.nameRight = nameRight;
	}

	public boolean isReplaceMatchesWithSolids() {
		return replaceMatchesWithSolids;
	}

	public void setReplaceMatchesWithSolids(boolean replaceMatchesWithSolids) {
		this.replaceMatchesWithSolids = replaceMatchesWithSolids;
	}
	
	@Override
	public boolean hasVeryQuickPerInputExecution() {
		return false;
	}
}
