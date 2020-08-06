package org.bonitasoft.maven;

import org.apache.maven.model.Dependency;

import java.util.List;
import java.util.Objects;
import java.util.Set;

import static java.util.stream.Collectors.toList;

/**
 * @author Benjamin Bentmann
 */
class ArtifactSelector {

    private Set<String> includes;
    private List<Dependency> dependencies;

    public ArtifactSelector(List<Dependency> dependencies, Set<String> includes) {
        this.dependencies = dependencies;
        this.includes = includes;
    }

    public List<Dependency> getDependencies() {
        return dependencies.stream().filter(dep ->
                includes.stream().anyMatch(filter ->
                        Objects.equals(dep.getGroupId() + ":" + dep.getArtifactId(), filter)
                )
        ).collect(toList());
    }
}