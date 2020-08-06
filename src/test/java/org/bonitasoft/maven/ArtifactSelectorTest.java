package org.bonitasoft.maven;

import org.apache.maven.model.Dependency;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class ArtifactSelectorTest {

    private ArtifactSelector artifactSelector;
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
        Dependency myConnector = new Dependency();
        myConnector.setGroupId("com.company");
        myConnector.setArtifactId("my-connector");
        myConnector.setVersion("1.1.0");
        dependencies.add(myConnector);

        Dependency johnny = new Dependency();
        johnny.setGroupId("dep");
        johnny.setArtifactId("johnny-dep");
        johnny.setVersion("0.0.0-SNAPSHOT");
        dependencies.add(johnny);

        includes.add("com.company:my-connector");

        artifactSelector = new ArtifactSelector(dependencies, includes);

        // When
        List<Dependency> dependencies = artifactSelector.getDependencies();

        // Then
        assertThat(dependencies).containsExactly(myConnector);
    }
}