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
import org.apache.commons.io.FileUtils;
import org.apache.maven.plugin.MojoExecutionException;

import java.io.File;
import java.io.IOException;

/**
 * Clean the project.
 *
 * @goal clean
 * @phase clean
 */
public class Play2CleanMojo
        extends AbstractPlay2Mojo {

    /**
     * Where are the dependencies copied.
     *
     * @parameter default-value="lib"
     */
    private File lib;

    /**
     * Set to false to avoid to clean the lib folder..
     *
     * @parameter default-value="true"
     */
    private boolean cleanLibFolder = true;

    public void execute()
            throws MojoExecutionException {

        String line = getPlay2().getAbsolutePath();

        CommandLine cmdLine = CommandLine.parse(line);
        cmdLine.addArgument("clean");
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
            throw new MojoExecutionException("Error during cleanup", e);
        }
        
        // Also delete the dist directory
        File dist = new File(project.getBasedir(), "dist");
        if (dist.exists()) {
            getLog().debug("Deleting " + dist.getAbsolutePath());
            try {
                FileUtils.deleteDirectory(dist);
            } catch (IOException e) {
                throw new MojoExecutionException("Can't delete the dist folder", e);
            }
        } else {
            getLog().debug("'dist' directory not found");
        }

        // Delete the log folder
        File logs = new File(project.getBasedir(), "logs");
        if (logs.exists()) {
            getLog().debug("Deleting " + logs.getAbsolutePath());
            try {
                FileUtils.deleteDirectory(logs);
            } catch (IOException e) {
                throw new MojoExecutionException("Can't delete the logs folder", e);
            }
        } else {
            getLog().debug("'logs' directory not found");
        }

        // Also delete the lib directory if set
        if (cleanLibFolder) {
            File lib = new File(project.getBasedir(), "lib");
            if (lib.exists()) {
                getLog().debug("Deleting " + lib.getAbsolutePath());
                try {
                    FileUtils.deleteDirectory(lib);
                } catch (IOException e) {
                    throw new MojoExecutionException("Can't delete the " + lib + " folder", e);
                }
            } else {
                getLog().debug("'" + lib + "' directory not found");
            }
        }
    }
}
