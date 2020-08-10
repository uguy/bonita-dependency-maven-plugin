package org.bonitasoft.maven.connector;

import org.apache.maven.model.Dependency;
import org.apache.maven.plugin.MojoExecutionException;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;

import static org.assertj.core.api.Assertions.assertThat;

class BonitaConnectorDependencyMojoTest {

    private BonitaConnectorDependencyMojo mojo;

    @BeforeEach
    void setUp() throws IOException {

        mojo = new BonitaConnectorDependencyMojo();
        mojo.setBuildDirectory(new File("target/test-project-root/target"));

        if(mojo.getBuildDirectory().exists()){
            Files.walk(mojo.getBuildDirectory().toPath())
                    .sorted(Comparator.reverseOrder())
                    .map(Path::toFile)
                    .filter(item -> !item.getPath().equals(mojo.getBuildDirectory().getAbsolutePath()))
                    .forEach(File::delete);
        }
    }

    @Test
    void dependency_should_be_unpacked() throws MojoExecutionException {
        // Given
        String zipName = "connector-starwars-1.0.0-SNAPSHOT";
        File zip = new File("src/test/resources/" + zipName + ".zip");
        File buildDirectory = mojo.getBuildDirectory();
        // When
        Path path = mojo.unpackDependency(zip,buildDirectory.toPath());

        // Then
        assertThat(path).exists();
        assertThat(path.getFileName().toString()).isEqualTo(zipName);
        assertThat(path.toFile().listFiles()).isNotEmpty();
    }

    @Test
    void resources_should_be_moved() throws MojoExecutionException {
        // Given
        String connectorName = "connector-starwars";
        String zipName = connectorName+"-1.0.0-SNAPSHOT";
        File zip = new File("src/test/resources/" + zipName + ".zip");
        Path buildDirectory = mojo.getBuildDirectory().toPath();
        Path path = mojo.unpackDependency(zip,buildDirectory);

        // When
        mojo.moveToFolder(path.resolve(connectorName+".def"), buildDirectory.resolve("../" + mojo.getConnector().getDefinitionFolder()));
        mojo.moveToFolder(path.resolve(connectorName+".impl"),buildDirectory.resolve("../" + mojo.getConnector().getImplementationFolder()));

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

        DefaultArtifact artifact = mojo.toArtifact(dependency);

        assertThat(artifact.getGroupId()).isEqualTo(dependency.getGroupId());
        assertThat(artifact.getArtifactId()).isEqualTo(dependency.getArtifactId());
        assertThat(artifact.getVersion()).isEqualTo(dependency.getVersion());
        assertThat(artifact.getExtension()).isEqualTo(dependency.getType());
    }
}