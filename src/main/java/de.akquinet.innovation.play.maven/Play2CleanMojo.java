package de.akquinet.innovation.play.maven;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteWatchdog;
import org.apache.commons.io.FileUtils;
import org.apache.maven.plugin.MojoExecutionException;

import java.io.File;
import java.io.IOException;

/**
 * Clean the project.
 *
 * @goal clean
 * @phase clean
 */
public class Play2CleanMojo
        extends AbstractPlay2Mojo {

    /**
     * Where are the dependencies copied.
     *
     * @parameter default-value="lib"
     */
    private File lib;

    /**
     * Set to false to avoid to clean the lib folder..
     *
     * @parameter default-value="true"
     */
    private boolean cleanLibFolder = true;

    public void execute()
            throws MojoExecutionException {

        String line = getPlay2().getAbsolutePath();

        CommandLine cmdLine = CommandLine.parse(line);
        cmdLine.addArgument("clean");
        DefaultExecutor executor = new DefaultExecutor();

        ExecuteWatchdog watchdog = new ExecuteWatchdog(120000); // 2min, even for Scala should be ok
        executor.setWatchdog(watchdog);

        executor.setExitValue(0);
        try {
            executor.execute(cmdLine);
        } catch (IOException e) {
            throw new MojoExecutionException("Error during compilation", e);
        }
        
        // Also delete the dist directory
        File dist = new File(project.getBasedir(), "dist");
        if (dist.exists()) {
            getLog().debug("Deleting " + dist.getAbsolutePath());
            try {
                FileUtils.deleteDirectory(dist);
            } catch (IOException e) {
                throw new MojoExecutionException("Can't delete the dist folder", e);
            }
        } else {
            getLog().debug("'dist' directory not found");
        }

        // Also delete the lib directory if set
        if (cleanLibFolder) {
            File lib = new File(project.getBasedir(), "lib");
            if (lib.exists()) {
                getLog().debug("Deleting " + lib.getAbsolutePath());
                try {
                    FileUtils.deleteDirectory(lib);
                } catch (IOException e) {
                    throw new MojoExecutionException("Can't delete the " + lib + " folder", e);
                }
            } else {
                getLog().debug("'" + lib + "' directory not found");
            }
        }
    }
}
