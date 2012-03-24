package de.akquinet.innovation.play.maven;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteWatchdog;
import org.apache.maven.plugin.MojoExecutionException;

import java.io.IOException;

/**
 * Launch the Play application
 *
 * @goal run
 * @requiresProject false
 * @requiresDependencyResolution provided
 *
 */
public class Play2RunMojo
        extends AbstractPlay2Mojo {

    public void execute()
            throws MojoExecutionException {

        String line = getPlay2().getAbsolutePath();

        CommandLine cmdLine = CommandLine.parse(line);
        cmdLine.addArgument("run");
        DefaultExecutor executor = new DefaultExecutor();

        executor.setExitValue(0);
        try {
            executor.execute(cmdLine);
        } catch (IOException e) {
            // Ignore.
        }
    }
}
