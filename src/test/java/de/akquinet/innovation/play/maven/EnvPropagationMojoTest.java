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

import static org.fest.assertions.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * Test the Packaging Mojo.
 */
public class EnvPropagationMojoTest {

    @Test
    public void testPackagingOfMavenAppWithCustomGAV() throws IOException, MojoExecutionException {
        if (! Helper.detectPlay2()) {
            System.err.println("PLAY2_HOME missing, skipping tests");
            return;
        }

        File baseDir = new File("target/tests/testPackagingOfMavenAppWithCustomGAV");
        Helper.copyMavenApp(baseDir);

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
        when(build.getFinalName()).thenReturn("my-artifact-id-1.0.0");
        when(mojo.project.getArtifactId()).thenReturn("my-artifact-id");
        when(mojo.project.getGroupId()).thenReturn("my-group-id");
        when(mojo.project.getVersion()).thenReturn("1.0.0");

        mojo.execute();

        // Verify attached file
        File target = new File(baseDir, "target");
        File dist = new File(target, "my-artifact-id-1.0.0.zip");
        File pack = new File(target, "my-artifact-id-1.0.0.jar");
        assertThat(dist).exists();
        assertThat(pack).exists();
        verify(artifact).setFile(pack);
        verify(mojo.projectHelper).attachArtifact(mojo.project, "zip", null, dist);
    }


}
