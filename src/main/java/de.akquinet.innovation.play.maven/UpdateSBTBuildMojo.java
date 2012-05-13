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
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Updates the SBT Build to keep synced Maven and SBT.
 * This mojo updates <tt>project/Build.scala</tt> to replace <tt>appName</tt> and <tt>appVersion</tt> by the artifact id
 * Maven version.
 *
 * @goal update-sbt-build
 * @since 1.2.0
 */
public class UpdateSBTBuildMojo
        extends AbstractMojo {

    /**
     * The maven project.
     *
     * @parameter expression="${project}"
     * @required
     * @readonly
     */
    protected MavenProject project;

    /**
     * To skip the metadata syncing.
     *
     * @parameter default-value="false" expression="${skipMetadataSync}"
     */
    private boolean skipMetadataSync;


    public void execute()
            throws MojoExecutionException {
        if (skipMetadataSync) {
            getLog().info("Skipping SBT <-> Maven sync");
            return;
        }
        // Find the SBT file
        File sbt = new File(project.getBasedir(), "project/Build.scala");
        if (! sbt.exists()) {
            getLog().warn("Can't find Build.scala");
            return;
        }


        List<String> lines = new ArrayList<String>();
        List<String> nlines = new ArrayList<String>();

        try {
            getLog().info("Reading Build.scala to update metadata");
            lines = FileUtils.readLines(sbt);
        } catch (IOException e) {
            getLog().error("Can't read the Build.scala file", e);
        }

        boolean modified = false;
        for (String line : lines) {
            String l = line;

            l = manageAppName(l);
            modified = modified || ! l.equals(line);

            l = manageVersion(l);
            modified = modified || ! l.equals(line);

            nlines.add(l);
        }

        if (modified) {
            try {
               getLog().info("Writting Build.scala with udpated metadata");
               FileUtils.writeLines(sbt, nlines);
            } catch (IOException e) {
                getLog().error("Can't write the Build.scala file", e);
            }
        } else {
            getLog().info("No update required in Build.scala.");
        }
    }

    public String manageAppName(String line) {
        Pattern pattern = Pattern.compile("(\\s*val\\s*appName\\s*=\\s*\")(.*)(\".*)");
        Matcher matcher = pattern.matcher(line);
        if (matcher.matches()) {
            String begin = matcher.group(1);
            String end = matcher.group(3);
            line = begin + project.getArtifactId() + end;
        }
        return line;
    }

    public String manageVersion(String line) {
        Pattern pattern = Pattern.compile("(\\s*val\\s*appVersion\\s*=\\s*\")(.*)(\".*)");
        Matcher matcher = pattern.matcher(line);
        if (matcher.matches()) {
            String begin = matcher.group(1);
            String end = matcher.group(3);
            line = begin + project.getVersion() + end;
        }
        return line;
    }


}
