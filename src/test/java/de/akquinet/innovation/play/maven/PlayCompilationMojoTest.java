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

import org.apache.commons.io.FileUtils;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Test the CompilationMojo.
 */
public class PlayCompilationMojoTest {

    @Test
    public void testCompilationOfJavaApplication() throws IOException, MojoExecutionException {
        if (! Helper.detectPlay2()) {
            System.err.println("PLAY2_HOME missing, skipping tests");
            return;
        }

        File baseDir = new File("target/tests/testCompilationOfJavaApplication");
        Helper.copyJavaApp(baseDir);

        Play2CompilationMojo mojo = new Play2CompilationMojo();
        mojo.project = mock(MavenProject.class);
        when(mojo.project.getBasedir()).thenReturn(baseDir);
        when(mojo.project.getArtifactId()).thenReturn("my-artifact-id");
        when(mojo.project.getGroupId()).thenReturn("my-group-id");
        when(mojo.project.getVersion()).thenReturn("0.0.1");

        mojo.execute();
    }

    @Test
    public void testCompilationOfScalaApplication() throws IOException, MojoExecutionException {
        if (! Helper.detectPlay2()) {
            System.err.println("PLAY2_HOME missing, skipping tests");
            return;
        }

        File baseDir = new File("target/tests/testCompilationOfScalaApplication");
        Helper.copyScalaApp(baseDir);

        Play2CompilationMojo mojo = new Play2CompilationMojo();
        mojo.project = mock(MavenProject.class);
        when(mojo.project.getBasedir()).thenReturn(baseDir);
        when(mojo.project.getArtifactId()).thenReturn("my-artifact-id");
        when(mojo.project.getGroupId()).thenReturn("my-group-id");
        when(mojo.project.getVersion()).thenReturn("0.0.1");

        mojo.execute();
    }
}
