package de.akquinet.innovation.play.maven;

import de.akquinet.innovation.play.maven.utils.CopyDependenciesEmbeddedMojo;
import org.apache.commons.io.FileUtils;
import org.apache.maven.archiver.MavenArchiveConfiguration;
import org.apache.maven.archiver.MavenArchiver;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.factory.ArtifactFactory;
import org.apache.maven.artifact.metadata.ArtifactMetadataSource;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.resolver.ArtifactCollector;
import org.apache.maven.artifact.resolver.ArtifactResolver;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.shared.dependency.tree.DependencyNode;
import org.apache.maven.shared.dependency.tree.DependencyTreeBuilder;
import org.apache.maven.shared.dependency.tree.DependencyTreeBuilderException;
import org.codehaus.plexus.archiver.war.WarArchiver;
import org.codehaus.plexus.util.StringUtils;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

/**
 * Packages the Play application as War.
 *
 * @goal package-war
 * @phase package
 * @requiresDependencyResolution test
 */
public class WarPackageMojo extends AbstractPlay2Mojo {

    private static final String META_INF = "META-INF";

    private static final String WEB_INF = "WEB-INF";

    public static final String TLD_PATH = "WEB-INF/tld/";

    public static final String SERVICES_PATH = "WEB-INF/services/";

    public static final String MODULES_PATH = "WEB-INF/modules/";

    public static final String EXTENSIONS_PATH = "WEB-INF/extensions/";

    public static final String CLASSES_PATH = "WEB-INF/classes/";

    public static final String LIB_PATH = "WEB-INF/lib/";

    /**
     * The directory where the webapp is built.
     *
     * @parameter default-value="${project.build.directory}/${project.build.finalName}"
     */
    File webappDirectory;

    /**
     * Dependencies of the current plugin.
     * This list is used to extract and copy the servlet bridge.
     *
     * @parameter expression="${plugin.artifacts}"
     */
    List pluginArtifacts;

    /**
     * Used to look up Artifacts in the remote repository.
     *
     * @component
     */
    protected ArtifactFactory factory;

    /**
     * Used to resolve Artifacts in the remote repository.
     *
     * @component
     */

    protected ArtifactResolver resolver;

    /**
     * Artifact collector, needed to resolve dependencies.
     *
     * @component role="org.apache.maven.artifact.resolver.ArtifactCollector"
     * @required
     * @readonly
     */
    protected ArtifactCollector artifactCollector;

    /**
     * The dependency tree builder to use.
     *
     * @component
     * @required
     * @readonly
     */
    protected DependencyTreeBuilder dependencyTreeBuilder;

    /**
     * @component role="org.apache.maven.artifact.metadata.ArtifactMetadataSource" hint="maven"
     * @required
     * @readonly
     */
    protected ArtifactMetadataSource artifactMetadataSource;

    /**
     * Location of the local repository.
     *
     * @parameter expression="${localRepository}"
     * @readonly
     * @required
     */
    protected ArtifactRepository local;

    /**
     * List of Remote Repositories used by the resolver
     *
     * @parameter expression="${project.remoteArtifactRepositories}"
     * @readonly
     * @required
     */
    protected List<ArtifactRepository> remoteRepos;

    /**
     * The WAR archiver.
     *
     * @component role="org.codehaus.plexus.archiver.Archiver" roleHint="war"
     */
    protected WarArchiver warArchiver;

    /**
     * Enables or disabled the packaging of the application as a War file.
     *
     * @parameter default-value=true
     */
    boolean buildWar;

    /**
     * Sets the war classifier.
     *
     * @parameter default-value=""
     */
    String warClassifier;

    /**
     * Allows customization of the play packaging. The files specified in this attribute will get added to the distribution
     * zip file. This allows, for example, to write your own start script and have it packaged in the distribution.
     * This is done post-packaging by the play framework.
     *
     * This parameter is shared with the package mojo.
     *
     * @parameter
     */
    List<String> additionalFiles = new ArrayList<String>();

    DependencyNode treeRoot;

    public void execute() throws MojoExecutionException, MojoFailureException {
        if (!buildWar) {
            getLog().info("Skipped War construction");
            return;
        }

        getLog().info("Build war file");

        prepareDependencyTree();

        try {
            // Create a temporary directory
            if (webappDirectory.exists()) {
                getLog().info(webappDirectory.getAbsolutePath() + " directory existing - deleting");
                FileUtils.deleteDirectory(webappDirectory);
            }

            // Prepare structure.
            prepareWarStructure();

            // Copy dependencies to the right folders.
            copyDependencies();
            copyPlayDependencies();

            // Copy class files, it must be the final class files built by Play.
            copyClassFiles();

            // Copy the servlet bridge.
            copyServletBridge();

            // Build the war file
            File warFile = new File(buildDirectory, project.getBuild().getFinalName() + ".war");
            packageWarFile(webappDirectory, warFile);
        } catch (IOException e) {
            getLog().error("Cannot build the War file : ", e);
            throw new MojoExecutionException("Error during War file construction", e);
        }

    }

    private void prepareDependencyTree() throws MojoExecutionException {
        try {
            getLog().debug("Building dependency tree");
            treeRoot =
                    dependencyTreeBuilder.buildDependencyTree(project, local, factory,
                            artifactMetadataSource, null, artifactCollector);
        } catch (DependencyTreeBuilderException e) {
            getLog().error("Cannot build the dependency tree", e);
            throw new MojoExecutionException("Cannot build the dependency tree", e);
        }
    }

    public File getWebappDirectory() {
        return webappDirectory;
    }

    public ArtifactFactory getFactory() {
        return factory;
    }

    public ArtifactResolver getResolver() {
        return resolver;
    }

    public ArtifactCollector getArtifactCollector() {
        return artifactCollector;
    }

    public ArtifactMetadataSource getArtifactMetadataSource() {
        return artifactMetadataSource;
    }

    public ArtifactRepository getLocal() {
        return local;
    }

    public List<ArtifactRepository> getRemoteRepos() {
        return remoteRepos;
    }

    public String getWarClassifier() {
        return warClassifier;
    }

    private void copyPlayDependencies() throws IOException {
        getLog().info("Copying Play runtime and its dependencies");
        CopyDependenciesEmbeddedMojo copy = new CopyDependenciesEmbeddedMojo(this, "play-test");
        try {
            copy.execute();
        } catch (MojoExecutionException e) {
            getLog().error("Cannot copy play runtime", e);
            throw new IOException("Error during the resolution of Play 2 dependencies", e);
        }

    }

    private void copyServletBridge() throws IOException {
        // We need to copy two artifacts : play2-war-core-common_2.9.1 and play2-war-core-servlet30_2.9.1
        List<Artifact> artifacts = pluginArtifacts;
        URL common = getUrlByArtifactId(artifacts, "play2-war-core-common_2.9.1");
        URL servlet = getUrlByArtifactId(artifacts, "play2-war-core-servlet30_2.9.1");

        FileUtils.copyURLToFile(common, new File(webappDirectory, LIB_PATH + "play2-war-core-common_2.9.1.jar"));
        FileUtils.copyURLToFile(servlet, new File(webappDirectory, LIB_PATH + "play2-war-core-servlet30_2.9.1.jar"));
    }

    /**
     * Gets the artifact's URL from the artifact list.
     *
     * @param artifacts  the list of artifact
     * @param artifactId the dependency artifact id.
     * @return the artifact's URL or <code>null</code> if the URL cannot
     *         be found.
     */
    private URL getUrlByArtifactId(List<Artifact> artifacts, String artifactId) {
        for (Artifact artifact : artifacts) {
            if (artifact.getArtifactId().equals(artifactId)) {
                try {
                    return artifact.getFile().toURI().toURL();
                } catch (MalformedURLException e) {
                    getLog().error("Cannot compute the url of the artifact : " + artifactId);
                }
            }
        }
        return null;
    }

    private void prepareWarStructure() {
        File webinfDir = new File(webappDirectory, WEB_INF);
        webinfDir.mkdirs();
        File metainfDir = new File(webappDirectory, META_INF);
        metainfDir.mkdirs();
    }

    private void copyClassFiles() throws IOException {
        File scala = findScalaDirectory();

        File classes = new File(scala, "classes");
        if (classes.exists()) {
            getLog().info("Copying classes from " + classes + " to " + CLASSES_PATH);
            FileUtils.copyDirectory(classes, new File(webappDirectory, CLASSES_PATH));
        }

        File managedClasses = new File(scala, "classes_managed");
        if (managedClasses.exists()) {
            getLog().info("Copying classes from " + managedClasses + " to " + CLASSES_PATH);
            FileUtils.copyDirectory(managedClasses, new File(webappDirectory, CLASSES_PATH));
        }

        File resourceClasses = new File(scala, "resource_managed");
        if (resourceClasses.exists()) {
            getLog().info("Copying resources from " + resourceClasses + " to " + CLASSES_PATH);
            FileUtils.copyDirectory(resourceClasses, new File(webappDirectory, CLASSES_PATH));
        }
    }

    private File findScalaDirectory() throws IOException {
        File[] array = buildDirectory.listFiles(new FilenameFilter() {
            public boolean accept(File file, String s) {
                return s.startsWith("scala-");
            }
        });

        if (array.length == 0) {
            throw new IOException("Cannot find Play output files");
        }
        if (array.length > 1) {
            throw new IOException("Cannot find Play output files - too many candidates");
        }
        return array[0];
    }

    private void packageWarFile(File war, File warFile) throws IOException {
        getLog().info("Build war file " + warFile.getAbsolutePath() + " from " + war.getAbsolutePath());
        // We build a Jar from the webappDirectory.
        MavenArchiver archiver = new MavenArchiver();
        archiver.setArchiver(warArchiver);
        archiver.setOutputFile(warFile);
        try {
            warArchiver.addDirectory(webappDirectory);

            // Manage additional files if any
            if (! additionalFiles.isEmpty()) {
                getLog().info("Adding additional files to War file : " + additionalFiles);
                for (String file : additionalFiles) {
                    File fileToAdd = new File(project.getBasedir(), file);
                    if (!fileToAdd.exists()) {
                        throw new IOException(fileToAdd.getCanonicalPath() + " not found, can't add to war file");
                    }
                    warArchiver.addFile(fileToAdd, fileToAdd.getName());
                }
            }


            warArchiver.setIgnoreWebxml(false);
            MavenArchiveConfiguration archive = new MavenArchiveConfiguration();
            archiver.createArchive(session, project, archive);
        } catch (Exception e) {
            getLog().error("Error during the construction of the War file with the archiving process", e);
            throw new IOException("Cannot build the War file", e);
        }

        if (!StringUtils.isBlank(warClassifier)) {
            projectHelper.attachArtifact(project, "war", warClassifier, warFile);
        } else {
            Artifact artifact = project.getArtifact();
            if (project.getFile() == null || !project.getFile().exists()) {
                artifact.setFile(warFile);
            } else {
                projectHelper.attachArtifact(project, "war", warFile);
            }
        }
    }

    private boolean mustBeEmbedded(Artifact artifact) {
        return !artifact.isOptional() &&
                EMBEDDED_SCOPES.contains(artifact.getScope()) &&
                !artifact.getArtifactId().contains("servlet-api");
    }

    private List<String> EMBEDDED_SCOPES = Arrays.asList("compile");


    public void copyDependencies()
            throws IOException {
        Set<Artifact> artifacts = project.getDependencyArtifacts();

        for (Artifact artifact : artifacts) {
            // The file name is just the artifact's file name.
            String targetFileName = artifact.getFile().getName();

            getLog().info("Processing: " + targetFileName);

            if (mustBeEmbedded(artifact)) {
                String type = artifact.getType();
                if ("tld".equals(type)) {
                    FileUtils.copyFile(artifact.getFile(), new File(webappDirectory, TLD_PATH + targetFileName));
                } else if ("aar".equals(type)) {
                    FileUtils.copyFile(artifact.getFile(), new File(webappDirectory, SERVICES_PATH + targetFileName));
                } else if ("mar".equals(type)) {
                    FileUtils.copyFile(artifact.getFile(), new File(webappDirectory, MODULES_PATH + targetFileName));
                } else if ("xar".equals(type)) {
                    FileUtils.copyFile(artifact.getFile(), new File(webappDirectory,
                            EXTENSIONS_PATH + targetFileName));
                } else if ("jar".equals(type) || "ejb".equals(type) || "ejb-client".equals(type)
                        || "test-jar".equals(type)) {
                    getLog().info("Copying " + targetFileName + " to " + LIB_PATH);
                    FileUtils.copyFile(artifact.getFile(), new File(webappDirectory,
                            LIB_PATH + targetFileName));
                } else if ("par".equals(type)) {
                    targetFileName = targetFileName.substring(0, targetFileName.lastIndexOf('.')) + ".jar";
                    FileUtils.copyFile(artifact.getFile(), new File(webappDirectory,
                            LIB_PATH + targetFileName));
                } else if ("war".equals(type)) {
                    getLog().warn("Not supported dependency type : war");
                } else if ("zip".equals(type)) {
                    getLog().warn("Not supported dependency type : zip");
                } else {
                    getLog().debug(
                            "Artifact of type [" + type + "] is not supported, ignoring [" + artifact + "]");
                }
            }
        }
    }

    public DependencyNode getDependencyTreeRoot() {
        return treeRoot;
    }
}
