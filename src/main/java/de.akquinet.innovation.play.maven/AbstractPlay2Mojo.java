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
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.HashSet;
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
     * Directory containing the build files.
     * @parameter expression="${project.build.directory}"
     */
    File buildDirectory;

    /**
     * Base directory of the project.
     * @parameter expression="${basedir}"
     */
    File baseDirectory;

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

    public static final String PLAY2_ARG_FORMAT = "-D%s=%s";

    /**
     * Stored the play 2 executable once found to avoid multiple searches.
     */
    private File play2executable;

    /**
     * Allows customization of the play execution System properties.
     * @parameter
     */
    Properties play2SystemProperties = new Properties();

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
     * @return the play2 location or <code>null</code> if not specified.
     */
    public String getPlay2Home() {
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

        return null;
    }

    public File getPlay2() throws MojoExecutionException {
        // Do we have a cached value ?
        if (play2executable != null) {
            return play2executable;
        }

        File play2 = null;

        // Either PLAY2_HOME is defined or not.
        // In the first case, we're looking for PLAY2_HOME/play[.bat]
        // In the second case we iterate over the PATH.
        String path = getPlay2Home();
        if (path != null) {
            if (isWindows()) {
                play2 = new File(path, "play.bat");
            } else {
                play2 = new File(path, "play");
            }
            if (play2.isFile()) {
                play2 = manageHomebrew(play2);
            } else {
                throw new MojoExecutionException(ENV_PLAY2_HOME + " system/configuration/environment variable is set " +
                        "to " + path + " but can't find the 'play' executable");
            }
        } else {
            getLog().info("Looking for 'play' in the System PATH");
            play2 = findPlay2ExecutableInSystemPath();
        }

        if (play2 == null || !play2.isFile()) {
            throw new MojoExecutionException("Can't find the 'play' executable. Set the " + ENV_PLAY2_HOME + " system/" +
                    "configuration/environment variable or check the the 'play' executable is available from the " +
                    "path");
        }

        getLog().debug("Using " + play2.getAbsolutePath());
        play2executable = play2;

        return play2;
    }

    /**
     * Checks whether the given play executable is in a <tt>Homebrew</tt> managed location.
     * Homebrew scripts seems to be an issue for play as the provided play executable from this directory is using a
     *  path expecting relative directories. So, we get such kind of error:
     *  <br/>
     * <code>
     *    /usr/local/Cellar/play/2.0/libexec/play: line 51:
     *    /usr/local/Cellar/play/2.0/libexec//usr/local/Cellar/play/2.0/libexec/../libexec/framework/build:
     *    No such file or directory
     * </code>
     * <br/>
     * In this case we substitute the play executable with the one installed by Homebrew but working correctly.
     * @param play2 the found play2 executable
     * @return the given play2 executable except if Homebrew is detected, in this case <tt>/usr/local/bin/play</tt>.
     */
    private File manageHomebrew(File play2) {
        if (play2.getAbsolutePath().contains("/Cellar/play/")) {
            getLog().info("Homebrew installation of play detected");
            // Substitute the play executable by the homebrew one.
            File file = new File("/usr/local/bin/play");
            if (! file.exists()) {
                getLog().error("Homebrew installation detected, but no play executable in /usr/local/bin");
            } else {
                return file;
            }
        }
        return play2;
    }

    private File findPlay2ExecutableInSystemPath() {
        String play2 = "play";
        if (isWindows()) {
            play2 = "play.bat";
        }
        String systemPath = System.getenv("PATH");

        // Fast failure if we don't have the PATH defined.
        if (systemPath == null) {
            return null;
        }

        String[] pathDirs = systemPath.split(File.pathSeparator);

        for (String pathDir : pathDirs) {
            File file = new File(pathDir, play2);
            if (file.isFile()) {
                return file;
            }
        }
        // Search not successful.
        return null;
    }

    /**
     * Checks whether the current operating system is Windows.
     * This check use the <tt>os.name</tt> system property.
     *
     * @return <code>true</code> if the os is windows, <code>false</code> otherwise.
     */
    /* package */ static boolean isWindows() {
        return System.getProperty("os.name").toLowerCase().contains("win");
    }

    /**
     * Gets the execution environment.
     * This method builds a map containing the Maven properties to give to SBT invocations.
     * It contains maven project data (GAV), pom properties, and system properties.
     * @return the map of properties (<code><pre>key -> value</pre></code>)
     */
    public Map<String, String> getEnvironment() {
        Map<String, String> env = new HashMap<String, String>();
        
        // Environment variables
        env.putAll( System.getenv() );

        // Build properties.
        env.put("project.groupId", project.getGroupId());
        env.put("project.artifactId", project.getArtifactId());
        env.put("project.version", project.getVersion());

        // Pom properties
        Properties props = project.getProperties();
        if (props != null) {
            for (Map.Entry<Object, Object> entry : props.entrySet()) {
                env.put(entry.getKey().toString(), entry.getValue().toString());
            }
        }

        // Environment properties
        Map<String, String> environment = System.getenv();
        for (String k : environment.keySet()) {
            env.put(k, environment.get(k));
        }
        
        // System properties
        props = System.getProperties();
        if (props != null) {
            for (Map.Entry<Object, Object> entry : props.entrySet()) {
                env.put(entry.getKey().toString(), entry.getValue().toString());
            }
        }
        
        getLog().debug("Environment built : " + env);
        return env;
    }

    /**
     * @return the maven project
     */
    public MavenProject getProject() {
        return project;
    }

    /**
     * @return the build directory (generally <tt>target</tt>)
     */
    public File getBuildDirectory() {
        return buildDirectory;
    }

    /**
     * @return the array of play2 system properties arguments.
     * The final execution line looks like: <tt>play -Dproperty=value -Dproperty2=value2 run/test</tt>
     */
    public String[] getPlay2SystemPropertiesArguments() {
        Set<String> args = new HashSet<String>();
        for (Map.Entry<Object, Object> entry : play2SystemProperties.entrySet()) {
            args.add(String.format(PLAY2_ARG_FORMAT, entry.getKey(), entry.getValue()));
        }
        return args.toArray(new String[0]);
    }
}
