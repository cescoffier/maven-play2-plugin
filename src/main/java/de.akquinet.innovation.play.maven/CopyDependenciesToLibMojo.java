package de.akquinet.innovation.play.maven;

import org.apache.maven.artifact.factory.ArtifactFactory;
import org.apache.maven.artifact.metadata.ArtifactMetadataSource;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.resolver.ArtifactCollector;
import org.apache.maven.artifact.resolver.ArtifactResolver;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.dependency.CopyDependenciesMojo;
import org.apache.maven.project.MavenProject;

import java.io.File;
import java.util.List;

/**
 * Copy project dependencies to the <tt>lib</tt> folder.
 *
 * @goal copy-dependencies
 * @requiresDependencyResolution test
 */
public class CopyDependenciesToLibMojo
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
     * Used to look up Artifacts in the remote repository.
     *
     * @component
     */
    protected ArtifactFactory factory;

    /**
     * Used to look up Artifacts in the remote repository.
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
     * @component role="org.apache.maven.artifact.metadata.ArtifactMetadataSource"
     * hint="maven"
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
    private ArtifactRepository local;

    /**
     * List of Remote Repositories used by the resolver
     *
     * @parameter expression="${project.remoteArtifactRepositories}"
     * @readonly
     * @required
     */
    protected List<ArtifactRepository> remoteRepos;

    /**
     * Where are the dependencies copied.
     *
     * @parameter default-value="lib"
     */
    private File lib;

    public void execute()
            throws MojoExecutionException {

        if (!lib.exists()) {
            lib.mkdirs();
        }

        CopyDependenciesMojo cdm = new PlayCopyDependenciesMojo();
        cdm.execute();
    }

    private class PlayCopyDependenciesMojo extends CopyDependenciesMojo {

        public PlayCopyDependenciesMojo() {
            super();
            project = CopyDependenciesToLibMojo.this.project;
            System.out.println("Factory : " + factory);
            setFactory(factory);
            setResolver(resolver);
            setArtifactCollector(artifactCollector);
            setArtifactMetadataSource(artifactMetadataSource);
            setLocal(local);
            setRemoteRepos(remoteRepos);
            setOutputDirectory(lib);
            setUseRepositoryLayout(false);
            setLog(getLog());
            setCopyPom(false);
            silent = false;
            overWriteIfNewer = true;
            overWriteSnapshots = true;
            overWriteReleases = false;
            excludeTransitive = false;
            excludeScope = "provided";
        }
    }
}
