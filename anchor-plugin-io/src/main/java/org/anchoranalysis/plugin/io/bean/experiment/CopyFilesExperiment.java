/* (C)2020 */
package org.anchoranalysis.plugin.io.bean.experiment;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Optional;
import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.core.log.Logger;
import org.anchoranalysis.core.log.MessageLogger;
import org.anchoranalysis.core.progress.ProgressReporter;
import org.anchoranalysis.core.progress.ProgressReporterConsole;
import org.anchoranalysis.core.progress.ProgressReporterNull;
import org.anchoranalysis.experiment.ExperimentExecutionArguments;
import org.anchoranalysis.experiment.ExperimentExecutionException;
import org.anchoranalysis.experiment.bean.Experiment;
import org.anchoranalysis.experiment.bean.identifier.ExperimentIdentifier;
import org.anchoranalysis.experiment.bean.log.LoggingDestination;
import org.anchoranalysis.experiment.bean.log.ToConsole;
import org.anchoranalysis.experiment.log.ConsoleMessageLogger;
import org.anchoranalysis.experiment.log.reporter.StatefulMessageLogger;
import org.anchoranalysis.io.bean.input.InputManagerParams;
import org.anchoranalysis.io.bean.provider.file.FileProvider;
import org.anchoranalysis.io.error.AnchorIOException;
import org.anchoranalysis.io.error.FileProviderException;
import org.anchoranalysis.io.output.bound.BoundOutputManager;
import org.anchoranalysis.plugin.io.bean.copyfilesmode.copymethod.CopyFilesMethod;
import org.anchoranalysis.plugin.io.bean.copyfilesmode.copymethod.SimpleCopy;
import org.anchoranalysis.plugin.io.bean.copyfilesmode.naming.CopyFilesNaming;
import org.anchoranalysis.plugin.io.bean.copyfilesmode.naming.PreserveName;
import org.anchoranalysis.plugin.io.bean.filepath.FilePath;
import org.apache.commons.io.FileUtils;

public class CopyFilesExperiment extends Experiment {

    // START BEAN PROPERTIES
    @BeanField @Getter @Setter private FileProvider fileProvider;

    @BeanField @Getter @Setter private FilePath sourceFolderPath;

    @BeanField @Getter @Setter private FilePath destinationFolderPath;

    @BeanField @Getter @Setter private boolean dummyMode = false;

    @BeanField @Getter @Setter private CopyFilesMethod copyFilesMethod = new SimpleCopy();

    @BeanField @Getter @Setter private CopyFilesNaming copyFilesNaming = new PreserveName();

    @BeanField @Getter @Setter private ExperimentIdentifier experimentIdentifier = null;

    @BeanField @Getter @Setter private boolean silentlyDeleteExisting = true;

    @BeanField @Getter @Setter private LoggingDestination log = new ToConsole();
    // END BEAN PROPERTIES

    @Override
    public void doExperiment(ExperimentExecutionArguments arguments)
            throws ExperimentExecutionException {

        // Determine a destination for the output, and create a corresponding logger
        Path destination = determineDestination(arguments.isDebugModeEnabled());
        StatefulMessageLogger logger = createLoggerFor(destination, arguments);

        logger.log("Reading files: ");

        try {
            doCopying(
                    findMatchingFiles(arguments),
                    sourceFolderPath.path(arguments.isDebugModeEnabled()),
                    destination,
                    logger);
            logger.close(true);
        } catch (AnchorIOException e) {
            logger.close(false);
            throw new ExperimentExecutionException(e);
        }
    }

    private Path determineDestination(boolean debugEnabled) throws ExperimentExecutionException {
        try {
            return destinationFolderPath.path(debugEnabled);
        } catch (AnchorIOException exc) {
            throw new ExperimentExecutionException("Cannot determine destination directory", exc);
        }
    }

    private StatefulMessageLogger createLoggerFor(
            Path destination, ExperimentExecutionArguments arguments) {
        return log.createWithConsoleFallback(
                new BoundOutputManager(destination, silentlyDeleteExisting), arguments, false);
    }

    @Override
    public boolean useDetailedLogging() {
        return true;
    }

    private void doCopying(
            Collection<File> files, Path sourcePath, Path destPath, MessageLogger logger)
            throws ExperimentExecutionException {

        ProgressReporter progressReporter = createProgressReporter(files.size());

        if (!dummyMode) {
            logger.log("Copying files: ");
            if (silentlyDeleteExisting) {
                FileUtils.deleteQuietly(destPath.toFile());
            }
            destPath.toFile().mkdirs();
        }

        progressReporter.open();

        try {
            copyFilesNaming.beforeCopying(destPath, files.size());

            int i = 0;
            for (File f : files) {
                copyFile(sourcePath, destPath, f, i++, progressReporter, logger);
            }

            copyFilesNaming.afterCopying(destPath, dummyMode);

        } catch (AnchorIOException | OperationFailedException e) {
            throw new ExperimentExecutionException(e);
        } finally {
            progressReporter.close();
        }
    }

    private ProgressReporter createProgressReporter(int numFiles) {
        ProgressReporter progressReporter =
                dummyMode ? ProgressReporterNull.get() : new ProgressReporterConsole(5);
        progressReporter.setMin(0);
        progressReporter.setMax(numFiles - 1);
        return progressReporter;
    }

    private void copyFile(
            Path sourcePath,
            Path destPath,
            File file,
            int iter,
            ProgressReporter progressReporter,
            MessageLogger logger)
            throws OperationFailedException {

        try {
            Optional<Path> destination =
                    copyFilesNaming.destinationPath(sourcePath, destPath, file, iter);

            // Skip any files with a NULL destinationPath
            if (!destination.isPresent()) {
                if (dummyMode) {
                    logger.logFormatted("Skipping %s%n", file.getPath());
                }
                return;
            }

            if (dummyMode) {
                logger.logFormatted("Copying %s to %s%n", file.getPath(), destination.toString());
            } else {
                copyFilesMethod.createDestinationFile(file.toPath(), destination.get());
            }
        } catch (AnchorIOException | CreateException e) {
            throw new OperationFailedException(e);
        } finally {
            progressReporter.update(iter);
        }
    }

    private Collection<File> findMatchingFiles(ExperimentExecutionArguments expArgs)
            throws ExperimentExecutionException {
        try {
            return fileProvider.create(
                    new InputManagerParams(
                            expArgs.createInputContext(),
                            new ProgressReporterConsole(5),
                            new Logger(new ConsoleMessageLogger()) // Print errors to the screen
                            ));
        } catch (FileProviderException e) {
            throw new ExperimentExecutionException("Cannot find input files", e);
        } catch (IOException e) {
            throw new ExperimentExecutionException("Cannot create input context", e);
        }
    }
}
