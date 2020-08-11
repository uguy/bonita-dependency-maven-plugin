package org.bonitasoft.maven.dependency;

import org.apache.maven.model.Dependency;
import org.apache.maven.plugin.MojoExecutionException;
import org.eclipse.aether.artifact.Artifact;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

public abstract class AbstractBonitaDependencyMojoTest {

    protected abstract AbstractBonitaDependencyMojo getMojo();

    protected abstract String getDefinitionFolder();

    protected abstract String getImplementationFolder();

    protected abstract String getLibFolder();

    @Test
    void dependency_should_be_unpacked() throws MojoExecutionException {
        // Given
        String zipName = "connector-starwars-1.0.0-SNAPSHOT";
        File zip = new File("src/test/resources/" + zipName + ".zip");
        File buildDirectory = getMojo().getProjectBuildDirectory();
        // When
        Path path = getMojo().unpackDependency(zip, buildDirectory.toPath());

        // Then
        assertThat(path).exists();
        assertThat(path.getFileName().toString()).isEqualTo(zipName);
        assertThat(path.toFile().listFiles()).isNotEmpty();
    }

    @Test
    void resources_should_be_moved() throws MojoExecutionException {
        // Given
        String connectorName = "connector-starwars";
        String zipName = connectorName + "-1.0.0-SNAPSHOT";
        File zip = new File("src/test/resources/" + zipName + ".zip");
        Path buildDirectory = getMojo().getProjectBuildDirectory().toPath();
        Path path = getMojo().unpackDependency(zip, buildDirectory);

        // When
        getMojo().copyToFolder(path.resolve(connectorName + ".def"), buildDirectory.resolve("../def"));
        getMojo().copyToFolder(path.resolve(connectorName + ".impl"), buildDirectory.resolve("../impl"));

        // Then
        assertThat(path).exists();
        assertThat(path.getFileName().toString()).isEqualTo(zipName);
        assertThat(path.toFile().listFiles()).isNotEmpty();
    }

    @Test
    void dep_should_match_artifact() {
        Dependency dependency = new Dependency();
        dependency.setGroupId("com.company.connector");
        dependency.setArtifactId("connector-starwars");
        dependency.setVersion("1.0.0-SNAPSHOT");
        dependency.setType("zip");

        Artifact artifact = getMojo().toArtifact(dependency);

        assertThat(artifact.getGroupId()).isEqualTo(dependency.getGroupId());
        assertThat(artifact.getArtifactId()).isEqualTo(dependency.getArtifactId());
        assertThat(artifact.getVersion()).isEqualTo(dependency.getVersion());
        assertThat(artifact.getExtension()).isEqualTo(dependency.getType());
    }

}