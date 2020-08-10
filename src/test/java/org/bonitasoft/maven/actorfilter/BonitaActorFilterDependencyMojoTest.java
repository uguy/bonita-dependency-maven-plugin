package org.bonitasoft.maven.actorfilter;

import org.bonitasoft.maven.AbstractBonitaDependencyMojo;
import org.bonitasoft.maven.AbstractBonitaDependencyMojoTest;
import org.junit.jupiter.api.BeforeEach;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;

class BonitaActorFilterDependencyMojoTest extends AbstractBonitaDependencyMojoTest {

    public static final String TEST_PROJECT_ROOT = "target/test-project-root";

    private BonitaActorFilterDependencyMojo mojo;

    @BeforeEach
    void setUp() throws IOException {

        mojo = new BonitaActorFilterDependencyMojo();
        mojo.setProjectDirectory(new File(TEST_PROJECT_ROOT));
        mojo.setBuildDirectory(new File(TEST_PROJECT_ROOT + "/target"));

        if (mojo.getBuildDirectory().exists()) {
            Files.walk(mojo.getBuildDirectory().toPath())
                    .sorted(Comparator.reverseOrder())
                    .map(Path::toFile)
                    .filter(item -> !item.getPath().equals(mojo.getBuildDirectory().getAbsolutePath()))
                    .forEach(File::delete);
        }
    }

    @Override
    protected AbstractBonitaDependencyMojo getMojo() {
        return mojo;
    }

    @Override
    protected String getDefinitionFolder() {
        return mojo.getActorFilter().getDefinitionFolder();
    }

    @Override
    protected String getImplementationFolder() {
        return mojo.getActorFilter().getImplementationFolder();
    }

    @Override
    protected String getLibFolder() {
        return mojo.getActorFilter().getLibFolder();
    }

}