package org.bonitasoft.maven.dependency;

import org.apache.maven.model.Dependency;

import java.util.List;
import java.util.Set;

import static java.util.stream.Collectors.toList;

/**
 * Dependency selector class. Select a subset of dependencies based on includes pattern.
 */
public class DependencySelector {

    /**
     * The includes patterns. Include pattern must follow the form 'groupId:artifactId' where artifactId can be '*' to match all from groupId.
     */
    private Set<String> includes;
    /**
     * The dependencies to filter
     */
    private List<Dependency> dependencies;

    public DependencySelector(List<Dependency> dependencies, Set<String> includes) {
        this.dependencies = dependencies;
        this.includes = includes;
    }

    /**
     * Return a subset of dependencies based on the given includes pattern.
     *
     * @return the selected dependency list
     */
    public List<Dependency> select() {
        return dependencies.stream().filter(dependency ->
                includes.stream().anyMatch(include -> {
                    String[] includeParts = include.split(":");
                    if (includeParts.length > 2) {
                        throw new RuntimeException("Include can only be 'groupId:artifactId' where artifactId can be '*' to match all ");
                    }
                    String groupId = includeParts[0];
                    String artifactId = includeParts[1];
                    return groupId.equals(dependency.getGroupId()) &&
                            ("*".equals(artifactId) || artifactId.equals(dependency.getArtifactId()));
                })
        ).collect(toList());
    }
}