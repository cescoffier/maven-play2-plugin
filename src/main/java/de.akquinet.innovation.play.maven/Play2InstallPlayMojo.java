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

import java.io.File;
import java.io.IOException;
import java.net.URL;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.codehaus.plexus.archiver.ArchiverException;
import org.codehaus.plexus.archiver.UnArchiver;
import org.codehaus.plexus.archiver.manager.ArchiverManager;
import org.codehaus.plexus.archiver.manager.NoSuchArchiverException;

/**
 * Download and install a Play! distribution.
 *
 * @goal install-play
 * @requiresProject false
 */
public class Play2InstallPlayMojo
        extends AbstractMojo
{

    /**
     * Maven ArchiverManager.
     * 
     * @component
     * @readonly
     */
    private ArchiverManager archiverManager;

    /**
     * Defines a Play! version to automatically install.
     * 
     * If not set, PLAY2_HOME detection takes place and Play! is not automatically installed.
     * If set the plugin will download and install specified Play! distribution.
     * See the <tt>play2basedir</tt> configuration paramenter to set an install directory.
     * 
     * @parameter expression="${play2version}"
     */
    private String play2version;

    /**
     * The directory where automatically installed Play! distributions are extracted.
     * 
     * It's highly recommended that you set this configuration parameter to a directory of your choice.
     * 
     * If not set its defaulted to project build directory. It means that a <tt>clean</tt> command will first downoad
     * and install specified Play! distribution and then wipe it. After that next plugin goal will download and install
     * again. It's not optimal but provides a default behavior that works.
     * If set the plugin will download Play! distribution in this directory and extract them inside it.
     * 
     * @parameter default-value="${project.build.directory}" expression="${play2basedir}"
     */
    private String play2basedir;

    public void execute()
            throws MojoExecutionException, MojoFailureException
    {
        if ( StringUtils.isEmpty( play2version ) ) {
            throw new MojoExecutionException( "play2version configuration parameter is not set" );
        }
        String debugLogPrefix = "AutoInstall - Play! " + play2version + ' ';
        File play2basedirFile = new File( play2basedir );
        File play2home = new File( play2basedirFile, "play-" + play2version );
        File play2 = new File( play2home, AbstractPlay2Mojo.isWindows() ? "play.bat" : "play" );

        // Is the requested Play! version already installed?
        if ( play2.isFile() && play2.canExecute() ) {

            getLog().info( debugLogPrefix + "is already installed in " + play2home );
            return;

        }

        getLog().info( "Play! " + play2version + " download and installation, please be patient ..." );
        File zipFile = new File( play2basedirFile, "play-" + play2version + ".zip" );

        try {

            URL zipUrl = new URL( "http://download.playframework.org/releases/play-" + play2version + ".zip" );
            FileUtils.forceMkdir( play2basedirFile );

            // Download
            getLog().debug( debugLogPrefix + "is downloading to " + zipFile );
            FileUtils.copyURLToFile( zipUrl, zipFile );

            // Extract
            getLog().debug( debugLogPrefix + "is extracting to " + play2basedir );
            UnArchiver unarchiver = archiverManager.getUnArchiver( zipFile );
            unarchiver.setSourceFile( zipFile );
            unarchiver.setDestDirectory( play2basedirFile );
            unarchiver.extract();

            // Prepare
            File framework = new File( play2home, "framework" );
            File build = new File( framework, AbstractPlay2Mojo.isWindows() ? "build.bat" : "build" );
            if ( !build.canExecute() && !build.setExecutable( true ) ) {
                throw new MojoExecutionException( "Can't set " + build + " execution bit" );
            }
            if ( !play2.canExecute() && !play2.setExecutable( true ) ) {
                throw new MojoExecutionException( "Can't set " + play2 + " execution bit" );
            }

            getLog().debug( debugLogPrefix + "is now installed in " + play2home );

        } catch ( NoSuchArchiverException ex ) {
            throw new MojoExecutionException( "Can't auto install Play! " + play2version + " in "
                                              + play2basedir, ex );
        } catch ( IOException ex ) {
            try {
                if ( play2home.exists() ) {
                    // Clean extracted data
                    FileUtils.forceDelete( play2home );
                }
            } catch ( IOException ignored ) {
                getLog().warn( "Unable to delete extracted Play! distribution after error: " + play2home );
            }
            throw new MojoExecutionException( "Can't auto install Play! " + play2version + " in "
                                              + play2basedir, ex );
        } catch (ArchiverException e) {
            throw new MojoExecutionException( "Cannot unzip Play " + play2version + " in "
                    + play2basedir, e );
        } finally {
            try {
                if ( zipFile.exists() ) {
                    // Clean downloaded data
                    FileUtils.forceDelete( zipFile );
                }
            } catch ( IOException ignored ) {
                getLog().warn( "Unable to delete downloaded Play! distribution: " + zipFile );
            }
        }
    }

}
