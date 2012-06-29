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

import java.io.File;
import java.io.IOException;

/**
 * Provide useful helper functions.
 */
public class Helper {
    //TODO Test clean mojo

    public static final File JAVA_APP_ROOT = new File("src/test/resources/java/app");
    public static final File SCALA_APP_ROOT = new File("src/test/resources/scala/app");
    public static final File MAVEN_APP_ROOT = new File("src/test/resources/sbt-properties/app");

    public static void copyJavaApp(File out) throws IOException {
        if (out.exists()) {
            FileUtils.deleteQuietly(out);
        }
        out.mkdirs();

        FileUtils.copyDirectory(JAVA_APP_ROOT, out);
    }

    public static void copyMavenApp(File out) throws IOException {
        if (out.exists()) {
            FileUtils.deleteQuietly(out);
        }
        out.mkdirs();

        FileUtils.copyDirectory(MAVEN_APP_ROOT, out);
    }

    public static void copyScalaApp(File out) throws IOException {
        if (out.exists()) {
            FileUtils.deleteQuietly(out);
        }
        out.mkdirs();

        FileUtils.copyDirectory(SCALA_APP_ROOT, out);
    }

    public static boolean detectPlay2() {
        String home = System.getProperty(AbstractPlay2Mojo.ENV_PLAY2_HOME);
        if (home != null  && home.length() != 0) {
            return true;
        }

        // Second check, environment variable
        home = System.getenv(AbstractPlay2Mojo.ENV_PLAY2_HOME);
        if (home != null  && home.length() != 0) {
            return true;
        }

        return false;
    }
}
