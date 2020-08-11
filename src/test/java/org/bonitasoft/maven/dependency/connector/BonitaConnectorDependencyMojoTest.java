package org.bonitasoft.maven.dependency.connector;

import org.apache.maven.project.MavenProject;
import org.bonitasoft.maven.dependency.AbstractBonitaDependencyMojo;
import org.bonitasoft.maven.dependency.AbstractBonitaDependencyMojoTest;
import org.junit.jupiter.api.BeforeEach;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;

class BonitaConnectorDependencyMojoTest extends AbstractBonitaDependencyMojoTest {


    public static final String TEST_PROJECT_ROOT = "target/test-project-root";

    private BonitaConnectorDependencyMojo mojo;

    @BeforeEach
    void setUp() throws IOException {

        mojo = new BonitaConnectorDependencyMojo();
        mojo.setProjectBaseDirectory(new File(TEST_PROJECT_ROOT));
        mojo.setProjectBuildDirectory(new File(TEST_PROJECT_ROOT + "/target"));

        if (mojo.getProjectBuildDirectory().exists()) {
            Files.walk(mojo.getProjectBuildDirectory().toPath())
                    .sorted(Comparator.reverseOrder())
                    .map(Path::toFile)
                    .filter(item -> !item.getPath().equals(mojo.getProjectBuildDirectory().getAbsolutePath()))
                    .forEach(File::delete);
        }
    }

    @Override
    protected AbstractBonitaDependencyMojo getMojo() {
        return mojo;
    }

    @Override
    protected String getDefinitionFolder() {
        return mojo.getConnector().getDefinitionFolder();
    }

    @Override
    protected String getImplementationFolder() {
        return mojo.getConnector().getImplementationFolder();
    }

    @Override
    protected String getLibFolder() {
        return mojo.getConnector().getLibFolder();
    }


}