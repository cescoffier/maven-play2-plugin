package de.akquinet.innovation.play.maven.utils;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.shared.dependency.tree.DependencyNode;
import org.apache.maven.shared.dependency.tree.traversal.DependencyNodeVisitor;

/**
 * Visitor checking if a specific artifact is <strong>not</strong> a dependency of a specific artifact
 * (specified using the artifact id).
 * The original use case was to exclude the transitive dependency of a specific artifact.
 * This visitor is made to visit the whole dependency tree to see if a given artifact is a (potentially transitive)
 * dependency of a specific artifact.
 */
public class IsNotADependencyOfArtifactVisitor implements DependencyNodeVisitor {
    private boolean result = false;
    private final Artifact test;
    private final String exclusionRootArtifactId;

    /**
     * Creates the visitor.
     *
     * @param exclusionRootArtifactId the artifact id of the artifact which `test` should not be a dependency of.
     * @param test                    the artifact to test
     */
    public IsNotADependencyOfArtifactVisitor(String exclusionRootArtifactId, Artifact test) {
        this.test = test;
        this.exclusionRootArtifactId = exclusionRootArtifactId;
    }

    /**
     * Checks whether an artifact <code>dependency</code> is a dependency of <code>parent</code> in the given
     * dependency tree
     * @param treeRoot the dependency tree
     * @param dependency the dependency to look up
     * @param parent the parent node (artifact), the node and all its children will be checked.
     * @return <code>true</code> if the artifact <code>dependency</code> is a dependency of <code>parent</code>
     */
    public static boolean isADependencyOf(DependencyNode treeRoot, Artifact dependency, String parent) {
        IsNotADependencyOfArtifactVisitor visitor = new IsNotADependencyOfArtifactVisitor(parent, dependency);
        treeRoot.accept(visitor);
        return visitor.isADependencyOfTheSpecifiedArtifact();
    }

    public boolean visit(DependencyNode node) {
        Artifact artifact = node.getArtifact();

        // We don't visit anything on play-test.
        if (test.getArtifactId().startsWith(exclusionRootArtifactId)) {
            return false;
        }

        if (test.equals(artifact)) {
            result = true;
            return false;
        }

        // We don't visit anything below the play-test node.
        if (node.getArtifact().getArtifactId().startsWith(exclusionRootArtifactId)) {
            return false;
        }
        return true;
    }

    public boolean endVisit(DependencyNode node) {
        return true;
    }

    public boolean isNotADependencyOfTheSpecifiedArtifact() {
        return result;
    }

    public boolean isADependencyOfTheSpecifiedArtifact() {
        return !result;
    }
}
