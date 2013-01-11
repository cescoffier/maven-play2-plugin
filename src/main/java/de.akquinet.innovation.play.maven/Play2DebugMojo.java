package de.akquinet.innovation.play.maven;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.maven.plugin.MojoExecutionException;

import java.io.IOException;

/**
 * Launch the Play application in debug mode (play debug run)
 *
 * @goal debug
 * @requiresProject false
 * @requiresDependencyResolution provided
 */
public class Play2DebugMojo extends AbstractPlay2Mojo {

    public void execute() throws MojoExecutionException {

        String line = getPlay2().getAbsolutePath();

        CommandLine cmdLine = CommandLine.parse(line);

        cmdLine.addArgument("debug");
        cmdLine.addArguments(getPlay2SystemPropertiesArguments(), false);
        cmdLine.addArgument("run");
        DefaultExecutor executor = new DefaultExecutor();

        // As where not linked to a project, we can't set the working directory.
        // So it will use the directory where mvn was launched.

        executor.setExitValue(0);
        try {
            executor.execute(cmdLine, getEnvironment());
        } catch (IOException e) {
            // Ignore.
        }
    }
}
