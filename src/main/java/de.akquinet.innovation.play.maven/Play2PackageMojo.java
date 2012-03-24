package de.akquinet.innovation.play.maven;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteWatchdog;
import org.apache.commons.io.FileUtils;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.MojoExecutionException;

import java.io.File;
import java.io.IOException;
import java.util.Collection;

/**
 * Package the Play application. The application is packaged as a zip using <tt>play dist</tt>.
 * The created application is attached to the project.
 *
 * @goal package
 * @phase package
 */
public class Play2PackageMojo
        extends AbstractPlay2Mojo {

    /**
     * Output file classifier.
     *
     * @parameter default-value=""
     */
    private String classifier;

    public void execute()
            throws MojoExecutionException {
        dist();
        File out = moveBuiltArtifactToTarget();
        addArtifactToProject(out);
    }

    private void dist() throws MojoExecutionException {
        String line = getPlay2().getAbsolutePath();

        CommandLine cmdLine = CommandLine.parse(line);
        cmdLine.addArgument("dist");
        DefaultExecutor executor = new DefaultExecutor();

        ExecuteWatchdog watchdog = new ExecuteWatchdog(120000);
        executor.setWatchdog(watchdog);

        executor.setExitValue(0);
        try {
            executor.execute(cmdLine);
        } catch (IOException e) {
            throw new MojoExecutionException("Error during compilation", e);
        }
    }

    private File moveBuiltArtifactToTarget() throws MojoExecutionException {
        // The artifact is in dist.
        File dist = new File(project.getBasedir(), "dist");
        if (!dist.exists()) {
            throw new MojoExecutionException("Can't find the 'dist' directory");
        }

        Collection<File> found = FileUtils.listFiles(dist, new String[]{"zip"}, false);
        if (found.size() == 0) {
            throw new MojoExecutionException("The distribution file was not found in " + dist.getAbsolutePath());
        } else if (found.size() > 1) {
            throw new MojoExecutionException("Too many match for the distribution file in " + dist.getAbsolutePath());
        }

        // 1 file
        File file = found.toArray(new File[0])[0];

        getLog().info("Distribution file found : " + file.getAbsolutePath());

        File target = new File(project.getBasedir(), "target");
        File out = new File(target, project.getBuild().getFinalName() + ".zip");

        try {
            getLog().info("Copying " + file.getName() + " to " + out.getName());
            FileUtils.copyFile(file, out, true);
        } catch (IOException e) {
            throw new MojoExecutionException("Can't copy the distribution file to the target folder", e);
        }

        return out;
    }

    private void addArtifactToProject(File out) {
        Artifact artifact = project.getArtifact();
        if (null == classifier || classifier.trim().length() == 0) {
            artifact.setFile(out);
        } else {
            projectHelper.attachArtifact(project, out, classifier);
        }
    }
}
