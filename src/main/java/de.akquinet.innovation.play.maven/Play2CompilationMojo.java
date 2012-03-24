package de.akquinet.innovation.play.maven;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteWatchdog;
import org.apache.maven.artifact.factory.ArtifactFactory;
import org.apache.maven.artifact.metadata.ArtifactMetadataSource;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.resolver.ArtifactCollector;
import org.apache.maven.artifact.resolver.ArtifactResolver;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.dependency.CopyDependenciesMojo;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Compile the Play application
 *
 * @goal compile
 * @phase compile
 */
public class Play2CompilationMojo
        extends AbstractPlay2Mojo {

    public void execute()
            throws MojoExecutionException {

        String line = getPlay2().getAbsolutePath();

        CommandLine cmdLine = CommandLine.parse(line);
        cmdLine.addArgument("compile");
        DefaultExecutor executor = new DefaultExecutor();

        ExecuteWatchdog watchdog = new ExecuteWatchdog(120000); // 2min, even for Scala should be ok
        executor.setWatchdog(watchdog);

        executor.setExitValue(0);
        try {
            executor.execute(cmdLine);
        } catch (IOException e) {
            throw new MojoExecutionException("Error during compilation", e);
        }
    }
}
