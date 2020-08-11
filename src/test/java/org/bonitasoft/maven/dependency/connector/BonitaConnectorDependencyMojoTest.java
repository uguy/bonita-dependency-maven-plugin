package org.bonitasoft.maven.dependency.connector;

import org.apache.maven.model.Dependency;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.bonitasoft.maven.dependency.AbstractBonitaDependencyMojo;
import org.bonitasoft.maven.dependency.AbstractBonitaDependencyMojoTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@RunWith(JUnitPlatform.class)
@ExtendWith(MockitoExtension.class)
class BonitaConnectorDependencyMojoTest extends AbstractBonitaDependencyMojoTest {


    public static final String TEST_PROJECT_ROOT = "target/test-project-root";

    private BonitaConnectorDependencyMojo mojo;

    @BeforeEach
    void setUp() throws Exception {

        mojo = spy(new BonitaConnectorDependencyMojo());
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

    @Test
    void mojo_should_unpack_dependencies_in_the_right_place() throws Exception {
        // Given
        MavenProject mavenProject = mock(MavenProject.class);
        Dependency myDep = new Dependency();
        myDep.setGroupId("com.company.connector");
        myDep.setArtifactId("connector-starwars");
        myDep.setVersion("1.0.0-SNAPSHOT");
        myDep.setScope("compile");
        myDep.setType("zip");
        lenient().when(mavenProject.getDependencies()).thenReturn(asList(myDep));

        mojo.setProject(mavenProject);

        Set<String> includes = new HashSet<>();
        includes.add("com.company.connector:*");
        mojo.getConnector().setIncludes(includes);

        lenient().doReturn(new File("src/test/resources/connector-starwars-1.0.0-SNAPSHOT.zip"))
                .when(mojo).resolveDependencyFile(eq(myDep));

        // When
        mojo.execute();

        // Then
        assertThat(mojo.getProjectBaseDirectory().toPath().resolve(Connector.DEFAULT_CONNECTOR_DEF_FOLDER)).exists();
    }


    @Test
    void new_configuration_should_return_default_configuration_values() {
        //Given
        //When
        Connector conf = new Connector();
        //Then
        assertThat(conf.getDefinitionFolder()).isEqualTo(Connector.DEFAULT_CONNECTOR_DEF_FOLDER);
        assertThat(conf.getImplementationFolder()).isEqualTo(Connector.DEFAULT_CONNECTOR_IMPL_FOLDER);
        assertThat(conf.getLibFolder()).isEqualTo(Connector.DEFAULT_LIB_FOLDER);
    }

    @Test
    void should_skip_execute_if_skip_set_to_true() throws MojoFailureException, MojoExecutionException {
        // Given
        mojo.setSkip(true);
        // When
        mojo.execute();
        // Then
        verify(mojo,never()).doExecute();
    }


    @Test
    void configuration_should_return_default_values_when_null() {
        //Given
        Connector conf = new Connector();
        //When
        conf.setDefinitionFolder(null);
        conf.setImplementationFolder(null);
        conf.setLibFolder(null);
        //Then
        assertThat(conf.getDefinitionFolder()).isEqualTo(Connector.DEFAULT_CONNECTOR_DEF_FOLDER);
        assertThat(conf.getImplementationFolder()).isEqualTo(Connector.DEFAULT_CONNECTOR_IMPL_FOLDER);
        assertThat(conf.getLibFolder()).isEqualTo(Connector.DEFAULT_LIB_FOLDER);
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