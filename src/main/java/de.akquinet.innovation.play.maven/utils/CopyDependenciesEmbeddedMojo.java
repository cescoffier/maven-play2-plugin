package de.akquinet.innovation.play.maven.utils;

import de.akquinet.innovation.play.maven.WarPackageMojo;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.dependency.CopyDependenciesMojo;
import org.apache.maven.plugin.dependency.utils.DependencyStatusSets;
import org.apache.maven.plugin.dependency.utils.DependencyUtil;
import org.apache.maven.shared.artifact.filter.collection.*;
import org.codehaus.plexus.util.StringUtils;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

public class CopyDependenciesEmbeddedMojo extends CopyDependenciesMojo {
    private final WarPackageMojo warPackageMojo;
    private final String excludeDependenciesOfArtifactId;

    public CopyDependenciesEmbeddedMojo(WarPackageMojo warPackageMojo, String excludeDependenciesOfArtifactId) {
        this.warPackageMojo = warPackageMojo;
        this.excludeDependenciesOfArtifactId = excludeDependenciesOfArtifactId;

        File lib = new File(warPackageMojo.getWebappDirectory(), WarPackageMojo.LIB_PATH);
        this.project = warPackageMojo.getProject();

        setFactory(warPackageMojo.getFactory());
        setResolver(warPackageMojo.getResolver());
        setArtifactCollector(warPackageMojo.getArtifactCollector());
        setArtifactMetadataSource(warPackageMojo.getArtifactMetadataSource());
        setLocal(warPackageMojo.getLocal());
        setRemoteRepos(warPackageMojo.getRemoteRepos());
        setUseRepositoryLayout(false);

        setLog(warPackageMojo.getLog());
        setCopyPom(false);
        this.silent = false;
        setFailOnMissingClassifierArtifact(false);
        setMarkersDirectory(new File(warPackageMojo.getBuildDirectory(), "markers"));
        outputAbsoluteArtifactFilename = false;
        setOutputDirectory(lib);
        overWriteIfNewer = true;
        overWriteSnapshots = true;
        overWriteReleases = false;
        excludeTransitive = false;
        setPrependGroupId(false);
        setStripVersion(false);
        setUseRepositoryLayout(false);
        type = "java-source";

        includeArtifactIds = "";
        includeClassifiers = "";
        includeGroupIds = "";
        includeScope = "";
        includeTypes = "";
        excludeArtifactIds = "";
        excludeClassifiers = "";
        excludeGroupIds = "";
        excludeTypes = "";
        excludeScope = "";
    }

    /**
     * Method creates filters and filters the projects dependencies. This method
     * also transforms the dependencies if classifier is set. The dependencies
     * are filtered in least specific to most specific order
     *
     * @param stopOnFailure
     * @return DependencyStatusSets - Bean of TreeSets that contains information
     *         on the projects dependencies
     * @throws org.apache.maven.plugin.MojoExecutionException
     *
     */
    protected DependencyStatusSets getDependencySets(boolean stopOnFailure)
            throws MojoExecutionException {
        // add filters in well known order, least specific to most specific
        FilterArtifacts filter = new FilterArtifacts();

        filter.addFilter(new ProjectTransitivityFilter(getProject().getDependencyArtifacts(), excludeTransitive));

        filter.addFilter(new ScopeFilter(DependencyUtil.cleanToBeTokenizedString(includeScope),
                DependencyUtil.cleanToBeTokenizedString(excludeScope)));

        filter.addFilter(new TypeFilter(DependencyUtil.cleanToBeTokenizedString(includeTypes),
                DependencyUtil.cleanToBeTokenizedString(excludeTypes)));

        filter.addFilter(new ClassifierFilter(DependencyUtil.cleanToBeTokenizedString(includeClassifiers),
                DependencyUtil.cleanToBeTokenizedString(excludeClassifiers)));

        filter.addFilter(new GroupIdFilter(DependencyUtil.cleanToBeTokenizedString(includeGroupIds),
                DependencyUtil.cleanToBeTokenizedString(excludeGroupIds)));

        filter.addFilter(new ArtifactIdFilter(DependencyUtil.cleanToBeTokenizedString(includeArtifactIds),
                DependencyUtil.cleanToBeTokenizedString(excludeArtifactIds)));

        filter.addFilter(new AbstractArtifactsFilter() {
            public Set filter(Set artifacts) throws ArtifactFilterException {
                Set result = new HashSet();
                for (Artifact artifact : (Set<Artifact>) artifacts) {
                    if (! IsNotADependencyOfArtifactVisitor.isADependencyOf(warPackageMojo.getDependencyTreeRoot(), artifact, excludeDependenciesOfArtifactId)) {
                        result.add(artifact);
                    }
                }
                return result;
            }
        });

        // start with all artifacts.
        Set<Artifact> artifacts = getProject().getArtifacts();

        // perform filtering
        try {
            artifacts = filter.filter(artifacts);
        } catch (ArtifactFilterException e) {
            throw new MojoExecutionException(e.getMessage(), e);
        }

        // transform artifacts if classifier is set
        DependencyStatusSets status = null;
        if (StringUtils.isNotEmpty(warPackageMojo.getWarClassifier())) {
            status = getClassifierTranslatedDependencies(artifacts, stopOnFailure);
        } else {
            status = filterMarkedDependencies(artifacts);
        }

        return status;
    }
}