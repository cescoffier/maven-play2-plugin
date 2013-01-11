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

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteWatchdog;
import org.apache.maven.plugin.MojoExecutionException;

import java.io.IOException;

/**
 * Run the test of the Play application.
 * The tests are run using <tt>play test</tt>
 *
 * @goal test
 * @phase test
 */
public class Play2TestMojo
        extends AbstractPlay2Mojo {

    /**
     * Set this to "true" to skip running tests, but still compile them. Its use is NOT RECOMMENDED, but quite
     * convenient on occasion.
     *
     * @parameter default-value="false" expression="${skipTests}"
     */
    private boolean skipTests;

    /**
     * Set this to "true" to bypass unit tests entirely. Its use is NOT RECOMMENDED, especially if you enable it using
     * the "maven.test.skip" property, because maven.test.skip disables both running the tests and compiling the tests.
     * Consider using the <code>skipTests</code> parameter instead.
     *
     * @parameter default-value="false" expression="${maven.test.skip}"
     */
    private boolean skip;

    /**
     * Set this to "true" to ignore a failure during testing. Its use is NOT RECOMMENDED, but quite convenient on
     * occasion.
     *
     * @parameter default-value="false" expression="${maven.test.failure.ignore}"
     */
    private boolean testFailureIgnore;

    public void execute()
            throws MojoExecutionException {

        if (isSkipExecution()) {
            getLog().info("Test phase skipped");
            return;
        }

        String line = getPlay2().getAbsolutePath();

        CommandLine cmdLine = CommandLine.parse(line);
        cmdLine.addArguments(getPlay2SystemPropertiesArguments(), false);
        cmdLine.addArgument("test");
        DefaultExecutor executor = new DefaultExecutor();

        if (timeout > 0) {
            ExecuteWatchdog watchdog = new ExecuteWatchdog(timeout);
            executor.setWatchdog(watchdog);
        }

        executor.setWorkingDirectory(project.getBasedir());

        executor.setExitValue(0);
        try {
            executor.execute(cmdLine, getEnvironment());
        } catch (IOException e) {
            if (testFailureIgnore) {
                getLog().error("Test execution failures ignored");
            } else {
                throw new MojoExecutionException("Error during compilation", e);
            }
        }
    }

    protected boolean isSkipExecution() {
        return skip || skipTests;
    }
}
