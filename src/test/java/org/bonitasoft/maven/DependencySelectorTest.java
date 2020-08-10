package org.bonitasoft.maven;

import org.apache.maven.model.Dependency;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class DependencySelectorTest {

    private DependencySelector dependencySelector;
    private Set<String> includes;
    private List<Dependency> dependencies;


    @BeforeEach
    void setUp() {
        includes = new HashSet<>();
        dependencies = new ArrayList<>();
    }

    @Test
    void dependenies_should_be_filtered() {

        // Given
        Dependency myConnectorDep = new Dependency();
        myConnectorDep.setGroupId("com.company");
        myConnectorDep.setArtifactId("my-connector");
        myConnectorDep.setVersion("1.1.0");
        dependencies.add(myConnectorDep);

        Dependency johnnyDep = new Dependency();
        johnnyDep.setGroupId("com.dep");
        johnnyDep.setArtifactId("johnny-dep");
        johnnyDep.setVersion("0.0.0-SNAPSHOT");
        dependencies.add(johnnyDep);

        includes.add("com.company:my-connector");

        dependencySelector = new DependencySelector(dependencies, includes);

        // When
        List<Dependency> dependencies = dependencySelector.select();

        // Then
        assertThat(dependencies).containsExactly(myConnectorDep);
    }


}