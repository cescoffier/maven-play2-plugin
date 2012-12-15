/*
 * Copyright 2012 akquinet
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.akquinet.innovation.play.maven;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.model.Build;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.SystemStreamLog;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.MavenProjectHelper;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import static org.fest.assertions.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

/**
 * Test the Packaging Mojo.
 */
public class PlayPackageMojoTest {

    /**
     * This test also checks the <tt>deleteDist</tt> option.
     * @throws IOException
     * @throws MojoExecutionException
     */
    @Test
    public void testPackagingOfJavaApplication() throws IOException, MojoExecutionException {
        if (! Helper.detectPlay2()) {
            System.err.println("PLAY2_HOME missing, skipping tests");
            return;
        }

        File baseDir = new File("target/tests/testPackagingOfJavaApplication");
        Helper.copyJavaApp(baseDir);

        Play2PackageMojo mojo = new Play2PackageMojo();

        mojo.project = mock(MavenProject.class);
        mojo.projectHelper = mock(MavenProjectHelper.class);
        mojo.attachDist = true;
        mojo.buildDist = true;
        mojo.deleteDist = true;
        mojo.setLog(new SystemStreamLog());
        Build build = mock(Build.class);
        Artifact artifact = mock(Artifact.class);

        when(mojo.project.getBasedir()).thenReturn(baseDir);
        when(mojo.project.getBuild()).thenReturn(build);
        when(mojo.project.getArtifact()).thenReturn(artifact);
        when(build.getFinalName()).thenReturn("app-1.0.0");
        when(mojo.project.getArtifactId()).thenReturn("my-artifact-id");
        when(mojo.project.getGroupId()).thenReturn("my-group-id");
        when(mojo.project.getVersion()).thenReturn("0.0.1");

        mojo.execute();

        // Verify attached file
        File target = new File(baseDir, "target");
        File dist = new File(target, "app-1.0.0.zip");
        File pack = new File(target, "app-1.0.0.jar");
        assertThat(dist).exists();
        assertThat(pack).exists();
        verify(artifact).setFile(pack);
        verify(mojo.projectHelper).attachArtifact(mojo.project, "zip", null, dist);

        // Assert deleteDist option behavior
        assertThat(new File(baseDir, "dist")).doesNotExist();
    }

    /**
     * This test checks that we can disable the <tt>deleteDist</tt> option.
     * @throws IOException
     * @throws MojoExecutionException
     */
    @Test
    public void testPackagingOfScalaApplication() throws IOException, MojoExecutionException {
        if (! Helper.detectPlay2()) {
            System.err.println("PLAY2_HOME missing, skipping tests");
            return;
        }

        File baseDir = new File("target/tests/testPackagingOfScalaApplication");
        Helper.copyScalaApp(baseDir);

        Play2PackageMojo mojo = new Play2PackageMojo();

        mojo.project = mock(MavenProject.class);
        mojo.projectHelper = mock(MavenProjectHelper.class);
        mojo.attachDist = true;
        mojo.buildDist = true;
        mojo.deleteDist = false;
        mojo.setLog(new SystemStreamLog());
        Build build = mock(Build.class);
        Artifact artifact = mock(Artifact.class);

        when(mojo.project.getBasedir()).thenReturn(baseDir);
        when(mojo.project.getBuild()).thenReturn(build);
        when(mojo.project.getArtifact()).thenReturn(artifact);
        when(build.getFinalName()).thenReturn("app-1.0.0");
        when(mojo.project.getArtifactId()).thenReturn("my-artifact-id");
        when(mojo.project.getGroupId()).thenReturn("my-group-id");
        when(mojo.project.getVersion()).thenReturn("0.0.1");

        mojo.execute();

        // Verify attached file
        File target = new File(baseDir, "target");
        File dist = new File(target, "app-1.0.0.zip");
        File pack = new File(target, "app-1.0.0.jar");
        assertThat(dist).exists();
        assertThat(pack).exists();
        verify(artifact).setFile(pack);
        verify(mojo.projectHelper).attachArtifact(mojo.project, "zip", null, dist);

        // Assert deleteDist option behavior
        assertThat(new File(baseDir, "dist")).exists();
    }

    /**
     * This test checks that additionalFiles get packaged in the distribution zip
     * @throws IOException
     * @throws MojoExecutionException
     */
    @Test
    public void testPackagingOfAdditionalFiles() throws IOException, MojoExecutionException {
        if (! Helper.detectPlay2()) {
            System.err.println("PLAY2_HOME missing, skipping tests");
            return;
        }

        File baseDir = new File("target/tests/testPackagingOfScalaApplication");
        Helper.copyScalaApp(baseDir);

        Play2PackageMojo mojo = new Play2PackageMojo();

        mojo.project = mock(MavenProject.class);
        mojo.projectHelper = mock(MavenProjectHelper.class);
        mojo.attachDist = true;
        mojo.buildDist = true;
        mojo.deleteDist = false;
        mojo.additionalFiles.add("src/test/resources/AdditionalFile.txt");
        mojo.setLog(new SystemStreamLog());
        Build build = mock(Build.class);
        Artifact artifact = mock(Artifact.class);

        when(mojo.project.getBasedir()).thenReturn(baseDir);
        when(mojo.project.getBuild()).thenReturn(build);
        when(mojo.project.getArtifact()).thenReturn(artifact);
        when(build.getFinalName()).thenReturn("app-1.0.0");
        when(mojo.project.getArtifactId()).thenReturn("my-artifact-id");
        when(mojo.project.getGroupId()).thenReturn("my-group-id");
        when(mojo.project.getVersion()).thenReturn("0.0.1");

        mojo.execute();

        // Verify attached file
        File target = new File(baseDir, "target");
        File dist = new File(target, "app-1.0.0.zip");
        assertThat(dist).exists();

        // Verify that the additional file was packaged correctly.
        ZipFile distZip = new ZipFile(dist);
        ZipEntry additionalFileEntry = distZip.getEntry("app-1.0.0/AdditionalFile.txt");
        assertThat(additionalFileEntry).isNotNull();
    }

    @Test
    public void testPackagingOfJavaApplicationWithClassifier() throws IOException, MojoExecutionException {
        if (! Helper.detectPlay2()) {
            System.err.println("PLAY2_HOME missing, skipping tests");
            return;
        }

        File baseDir = new File("target/tests/testPackagingOfJavaApplicationWithClassifier");
        Helper.copyJavaApp(baseDir);

        Play2PackageMojo mojo = new Play2PackageMojo();

        mojo.project = mock(MavenProject.class);
        mojo.projectHelper = mock(MavenProjectHelper.class);
        mojo.attachDist = true;
        mojo.buildDist = true;
        mojo.classifier = "play";
        mojo.setLog(new SystemStreamLog());
        Build build = mock(Build.class);
        Artifact artifact = mock(Artifact.class);

        when(mojo.project.getBasedir()).thenReturn(baseDir);
        when(mojo.project.getBuild()).thenReturn(build);
        when(mojo.project.getArtifact()).thenReturn(artifact);
        when(build.getFinalName()).thenReturn("app-1.0.0");
        when(mojo.project.getArtifactId()).thenReturn("my-artifact-id");
        when(mojo.project.getGroupId()).thenReturn("my-group-id");
        when(mojo.project.getVersion()).thenReturn("0.0.1");

        mojo.execute();

        // Verify attached file
        File target = new File(baseDir, "target");
        File dist = new File(target, "app-1.0.0-play.zip");
        File pack = new File(target, "app-1.0.0-play.jar");
        assertThat(dist).exists();
        assertThat(pack).exists();
        verify(mojo.projectHelper).attachArtifact(mojo.project, "jar", "play", pack);
        verify(mojo.projectHelper).attachArtifact(mojo.project, "zip", "play", dist);
    }

    @Test
    public void testPackagingOfScalaApplicationWithClassifier() throws IOException, MojoExecutionException {
        if (! Helper.detectPlay2()) {
            System.err.println("PLAY2_HOME missing, skipping tests");
            return;
        }

        File baseDir = new File("target/tests/testPackagingOfScalaApplicationWithClassifier");
        Helper.copyScalaApp(baseDir);

        Play2PackageMojo mojo = new Play2PackageMojo();

        mojo.project = mock(MavenProject.class);
        mojo.projectHelper = mock(MavenProjectHelper.class);
        mojo.attachDist = true;
        mojo.buildDist = true;
        mojo.classifier = "play";
        mojo.setLog(new SystemStreamLog());
        Build build = mock(Build.class);
        Artifact artifact = mock(Artifact.class);

        when(mojo.project.getBasedir()).thenReturn(baseDir);
        when(mojo.project.getBuild()).thenReturn(build);
        when(mojo.project.getArtifact()).thenReturn(artifact);
        when(build.getFinalName()).thenReturn("app-1.0.0");
        when(mojo.project.getArtifactId()).thenReturn("my-artifact-id");
        when(mojo.project.getGroupId()).thenReturn("my-group-id");
        when(mojo.project.getVersion()).thenReturn("0.0.1");

        mojo.execute();

        // Verify attached file
        File target = new File(baseDir, "target");
        File dist = new File(target, "app-1.0.0-play.zip");
        File pack = new File(target, "app-1.0.0-play.jar");
        assertThat(dist).exists();
        assertThat(pack).exists();
        verify(mojo.projectHelper).attachArtifact(mojo.project, "jar", "play", pack);
        verify(mojo.projectHelper).attachArtifact(mojo.project, "zip", "play", dist);
    }

    @Test
    public void testPackagingOfJavaApplicationPackageOnly() throws IOException, MojoExecutionException {
        if (! Helper.detectPlay2()) {
            System.err.println("PLAY2_HOME missing, skipping tests");
            return;
        }

        File baseDir = new File("target/tests/testPackagingOfJavaApplicationPackageOnly");
        Helper.copyJavaApp(baseDir);

        Play2PackageMojo mojo = new Play2PackageMojo();

        mojo.project = mock(MavenProject.class);
        mojo.projectHelper = mock(MavenProjectHelper.class);
        mojo.attachDist = false;
        mojo.buildDist = false;
        mojo.setLog(new SystemStreamLog());
        Build build = mock(Build.class);
        Artifact artifact = mock(Artifact.class);

        when(mojo.project.getBasedir()).thenReturn(baseDir);
        when(mojo.project.getBuild()).thenReturn(build);
        when(mojo.project.getArtifact()).thenReturn(artifact);
        when(build.getFinalName()).thenReturn("app-1.0.0");
        when(mojo.project.getArtifactId()).thenReturn("my-artifact-id");
        when(mojo.project.getGroupId()).thenReturn("my-group-id");
        when(mojo.project.getVersion()).thenReturn("0.0.1");

        mojo.execute();

        // Verify attached file
        File target = new File(baseDir, "target");
        File dist = new File(target, "app-1.0.0.zip");
        File pack = new File(target, "app-1.0.0.jar");
        assertThat(dist).doesNotExist();
        assertThat(pack).exists();
        verify(artifact).setFile(pack);
        verifyZeroInteractions(mojo.projectHelper);
    }

    @Test
    public void testPackagingOfScalaApplicationPackageOnly() throws IOException, MojoExecutionException {
        if (! Helper.detectPlay2()) {
            System.err.println("PLAY2_HOME missing, skipping tests");
            return;
        }

        File baseDir = new File("target/tests/testPackagingOfScalaApplicationPackageOnly");
        Helper.copyScalaApp(baseDir);

        Play2PackageMojo mojo = new Play2PackageMojo();

        mojo.project = mock(MavenProject.class);
        mojo.projectHelper = mock(MavenProjectHelper.class);
        mojo.attachDist = false;
        mojo.buildDist = false;
        mojo.setLog(new SystemStreamLog());
        Build build = mock(Build.class);
        Artifact artifact = mock(Artifact.class);

        when(mojo.project.getBasedir()).thenReturn(baseDir);
        when(mojo.project.getBuild()).thenReturn(build);
        when(mojo.project.getArtifact()).thenReturn(artifact);
        when(build.getFinalName()).thenReturn("app-1.0.0");
        when(mojo.project.getArtifactId()).thenReturn("my-artifact-id");
        when(mojo.project.getGroupId()).thenReturn("my-group-id");
        when(mojo.project.getVersion()).thenReturn("0.0.1");

        mojo.execute();

        // Verify attached file
        File target = new File(baseDir, "target");
        File dist = new File(target, "app-1.0.0.zip");
        File pack = new File(target, "app-1.0.0.jar");
        assertThat(dist).doesNotExist();
        assertThat(pack).exists();
        verify(artifact).setFile(pack);
        verifyZeroInteractions(mojo.projectHelper);
    }
}
