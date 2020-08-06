package org.bonitasoft.maven;

import org.apache.maven.model.Dependency;
import org.apache.maven.plugin.MojoExecutionException;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class BonitaDependencyMojoTest {


    BonitaDependencyMojo mojo = new BonitaDependencyMojo();


    @Test
    void dependency_should_be_unpacked() throws MojoExecutionException, IOException {

        Dependency myConnector = new Dependency();
        myConnector.setGroupId("com.company");
        myConnector.setArtifactId("my-connector");
        myConnector.setVersion("1.1.0");

        mojo.unpackDependency(myConnector);

//        assertThat()


    }
}