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

import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.MavenProjectHelper;

import java.io.File;

/**
 * Common parent of all Play 2 Mojo
 */
public abstract class AbstractPlay2Mojo extends AbstractMojo {

    /**
     * The maven project.
     *
     * @parameter expression="${project}"
     * @required
     * @readonly
     */
    MavenProject project;

    /**
     * The maven session.
     *
     * @parameter expression="${session}"
     * @required
     * @readonly
     */
    MavenSession session;

    /**
     * Maven ProjectHelper.
     *
     * @component
     * @readonly
     */
    MavenProjectHelper projectHelper;

    /**
     * The PLAY2_HOME path is taken from this setting, if not found as a Java system property (-DPLAY2_HOME).
     * Refers to the PLAY2_HOME environment variable by default.
     * <p/>
     * So that means that the PLAY2_HOME can be given using:
     * <ol>
     * <li>A system variable defined by the system or with <tt>-DPLAY2_HOME=...</tt></li>
     * <li>The <tt>play2Home</tt> configuration property</li>
     * <li>The PLAY2_HOME environment property</li>
     * </ol>
     *
     * @parameter expression="${env.PLAY2_HOME}"
     */
    String play2Home;

    /**
     * Sets a timeout to the <tt>play</tt> invocation (in milliseconds).
     * If not set (or set to <tt>-1</tt>, the plugin waits until the underlying <tt>play</tt> process completes.
     * If set, the plugin kills the underlying <tt>play</tt> process when the timeout is reached, and it fails the build.
     * @parameter default-value="-1" expression="${play2timeout}"
     */
    long timeout;


    public static final String ENV_PLAY2_HOME = "PLAY2_HOME";

    /**
     * Gets the specified <tt>PLAY2_HOME</tt> location.
     * This method checks in this order:
     * <ul>
     * <li>the PLAY2_HOME system variable</li>
     * <li>the <tt>play2Home</tt> settings</li>
     * <li>the PLAY2_HOME environment variable</li>
     * </ul>
     * If none is set, this method throws an exception.
     *
     * @return the play2 location
     * @throws MojoExecutionException if the play2 location is not defined.
     */
    public String getPlay2HomeOrThrow() throws MojoExecutionException {
        // First check, system variable
        String home = System.getProperty(ENV_PLAY2_HOME);
        if (home != null && !home.isEmpty()) {
            getLog().debug("Get Play2 home from system variable");
            return home;
        }

        // Second check, the setting configuration
        if (play2Home != null && !play2Home.isEmpty()) {
            getLog().debug("Get Play2 home from settings");
            return play2Home;
        }

        // Third check, environment variable
        home = System.getenv(ENV_PLAY2_HOME);
        if (home != null && !home.isEmpty()) {
            getLog().debug("Get Play2 home from environment");
            return home;
        }

        throw new MojoExecutionException(ENV_PLAY2_HOME + " system/configuration/environment variable not set");

    }

    public File getPlay2() throws MojoExecutionException {
        File play2 = null;
        String path = getPlay2HomeOrThrow();
        if (isWindows()) {
            play2 = new File(path, "play.bat");
        } else {
            play2 = new File(path, "play");
        }

        if (!play2.exists()) {
            throw new MojoExecutionException("Can't find the play executable in " + path);
        } else {
            getLog().debug("Using " + play2.getAbsolutePath());
        }

        return play2;
    }

    /**
     * Checks whether the current operating system is Windows.
     * This check use the <tt>os.name</tt> system property.
     *
     * @return <code>true</code> if the os is windows, <code>false</code> otherwise.
     */
    public boolean isWindows() {
        return System.getProperty("os.name").toLowerCase().indexOf("win") != -1;
    }

}
