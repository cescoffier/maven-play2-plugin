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

import org.apache.maven.project.MavenProject;
import org.junit.Test;
import org.mockito.Mockito;

import static org.fest.assertions.Assertions.assertThat;

/**
 * Checks the behavior of the Update SBT Build Mojo.
 */
public class UpdateSBTBuildMojoTest {

    @Test
    public void testAppNameReplacement() {
        UpdateSBTBuildMojo mojo = new UpdateSBTBuildMojo();

        MavenProject project = Mockito.mock(MavenProject.class);
        Mockito.when(project.getArtifactId()).thenReturn("artifactId");

        mojo.project = project;

        // Line that should not be modified.
        String line = "val appName         = \"artifactId\"";
        String updated = mojo.manageAppName(line);
        assertThat(updated).isEqualTo(line);

        // Empty line
        line = "val appName         = \"\"";
        updated = mojo.manageAppName(line);
        assertThat(updated).isNotEqualTo(line);
        assertThat(updated).contains("artifactId");

        // Must be sync
        line = "val appName         = \"not the same\"";
        updated = mojo.manageAppName(line);
        assertThat(updated).isNotEqualTo(line);
        assertThat(updated).contains("artifactId");

        // No match
        line = "val stuff         = \"something\"";
        updated = mojo.manageAppName(line);
        assertThat(updated).isEqualTo(line);
    }

    @Test
    public void testVersionReplacement() {
        UpdateSBTBuildMojo mojo = new UpdateSBTBuildMojo();

        MavenProject project = Mockito.mock(MavenProject.class);
        Mockito.when(project.getVersion()).thenReturn("1.0.0-SNAPSHOT");

        mojo.project = project;

        // Line that should not be modified.
        String line = "val appVersion         = \"1.0.0-SNAPSHOT\"";
        String updated = mojo.manageVersion(line);
        assertThat(updated).isEqualTo(line);

        // Empty line
        line = "val appVersion         = \"\"";
        updated = mojo.manageVersion(line);
        assertThat(updated).isNotEqualTo(line);
        assertThat(updated).contains("1.0.0-SNAPSHOT");

        // Must be sync
        line = "val appVersion         = \"not the same\"";
        updated = mojo.manageVersion(line);
        assertThat(updated).isNotEqualTo(line);
        assertThat(updated).contains("1.0.0-SNAPSHOT");

        // No match
        line = "val stuff         = \"something\"";
        updated = mojo.manageVersion(line);
        assertThat(updated).isEqualTo(line);
    }

}
