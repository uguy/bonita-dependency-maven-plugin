package org.bonitasoft.maven.connector;

import org.apache.maven.plugin.MojoExecutionException;
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
        mojo.setBuildDirectory(new File("target/test/"));

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

        // When
        Path path = mojo.unpackDependency(zip);

        // Then
        assertThat(path).exists();
        assertThat(path.getFileName().toString()).isEqualTo(zipName);
        assertThat(path.toFile().listFiles()).isNotEmpty();
    }
}