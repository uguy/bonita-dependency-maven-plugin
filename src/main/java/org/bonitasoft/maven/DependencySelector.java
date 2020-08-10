package org.bonitasoft.maven;

import org.apache.maven.model.Dependency;

import java.util.List;
import java.util.Set;

import static java.util.stream.Collectors.toList;

/**
 * @author Benjamin Bentmann
 */
public class DependencySelector {

    private Set<String> includes;
    private List<Dependency> dependencies;

    public DependencySelector(List<Dependency> dependencies, Set<String> includes) {
        this.dependencies = dependencies;
        this.includes = includes;
    }

    public List<Dependency> select() {
        return dependencies.stream().filter(dependency ->
                includes.stream().anyMatch(filter -> dependency.getManagementKey().startsWith(filter))
        ).collect(toList());
    }
}