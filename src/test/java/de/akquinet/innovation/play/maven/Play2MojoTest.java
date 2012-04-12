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

import org.apache.maven.plugin.MojoExecutionException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.fest.assertions.Assertions.assertThat;

/**
 * Checks the common methods of Play 2 mojos
 */
public class Play2MojoTest {

    private String old_play2_system_var;

    @Before
    public void setUp() {
        // Store the current configuration
        old_play2_system_var = System.getProperty(AbstractPlay2Mojo.ENV_PLAY2_HOME);
    }

    @After
    public void tearDown() {
        // Restore configuration
        if (old_play2_system_var != null) {
            System.setProperty(AbstractPlay2Mojo.ENV_PLAY2_HOME, old_play2_system_var);
        }
    }

    @Test
    public void testPlay2LocationUsingSystemVariable() throws MojoExecutionException {
        System.setProperty(AbstractPlay2Mojo.ENV_PLAY2_HOME, "somewhere");
        Play2CleanMojo mojo = new Play2CleanMojo();
        mojo.play2Home = "ignored";
        assertThat(mojo.getPlay2HomeOrThrow()).isEqualTo("somewhere");
    }

    @Test
    public void testPlay2LocationUsingSetting() throws MojoExecutionException {
        System.clearProperty(AbstractPlay2Mojo.ENV_PLAY2_HOME);

        // I don't really like this conditional test, but I can't be usre of the existence of the env variable.
        Play2CleanMojo mojo = new Play2CleanMojo();
        mojo.play2Home = "somewhere";

        assertThat(mojo.getPlay2HomeOrThrow()).isEqualTo("somewhere");
    }

    @Test
    public void testPlay2LocationUsingEnvVariable() throws MojoExecutionException {
        String env = System.getenv(AbstractPlay2Mojo.ENV_PLAY2_HOME);
        System.clearProperty(AbstractPlay2Mojo.ENV_PLAY2_HOME);

        // I don't really like this conditional test, but I can't be sure of the existence of the env variable.
        Play2CleanMojo mojo = new Play2CleanMojo();
        if (env == null) {
            mojo.play2Home = "somewhere";
            env = "somewhere";
        }

        assertThat(mojo.getPlay2HomeOrThrow()).isEqualTo(env);
    }

}
