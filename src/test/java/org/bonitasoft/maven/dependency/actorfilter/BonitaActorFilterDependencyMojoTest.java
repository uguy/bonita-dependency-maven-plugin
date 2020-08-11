package org.bonitasoft.maven.dependency.actorfilter;

import org.apache.maven.model.Dependency;
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
import static org.mockito.Mockito.*;

@RunWith(JUnitPlatform.class)
@ExtendWith(MockitoExtension.class)
class BonitaActorFilterDependencyMojoTest extends AbstractBonitaDependencyMojoTest {

    public static final String TEST_PROJECT_ROOT = "target/test-project-root";

    private BonitaActorFilterDependencyMojo mojo;

    @BeforeEach
    void setUp() throws Exception {

        mojo = spy(new BonitaActorFilterDependencyMojo());
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
        myDep.setGroupId("com.company.actorfilter");
        myDep.setArtifactId("sample-actorfilter");
        myDep.setVersion("1.0.0-SNAPSHOT");
        myDep.setScope("compile");
        myDep.setType("zip");
        lenient().when(mavenProject.getDependencies()).thenReturn(asList(myDep));

        mojo.setProject(mavenProject);

        Set<String> includes = new HashSet<>();
        includes.add("com.company.actorfilter:*");
        mojo.getActorFilter().setIncludes(includes);

        lenient().doReturn(new File("src/test/resources/sample-actorfilter-1.0.0-SNAPSHOT.zip"))
                .when(mojo).resolveDependencyFile(eq(myDep));

        // When
        mojo.execute();

        // Then
        assertThat(mojo.getProjectBaseDirectory().toPath().resolve(ActorFilter.DEFAULT_FILTER_DEF_FOLDER)).exists();
    }

    @Test
    void new_configuration_should_return_default_configuration_values() {
        //Given
        //When
        ActorFilter conf = new ActorFilter();
        //Then
        assertThat(conf.getDefinitionFolder()).isEqualTo(ActorFilter.DEFAULT_FILTER_DEF_FOLDER);
        assertThat(conf.getImplementationFolder()).isEqualTo(ActorFilter.DEFAULT_FILTER_IMPL_FOLDER);
        assertThat(conf.getLibFolder()).isEqualTo(ActorFilter.DEFAULT_LIB_FOLDER);
    }

    @Test
    void configuration_should_return_default_values_when_null() {
        //Given
        ActorFilter conf = new ActorFilter();
        //When
        conf.setDefinitionFolder(null);
        conf.setImplementationFolder(null);
        conf.setLibFolder(null);
        //Then
        assertThat(conf.getDefinitionFolder()).isEqualTo(ActorFilter.DEFAULT_FILTER_DEF_FOLDER);
        assertThat(conf.getImplementationFolder()).isEqualTo(ActorFilter.DEFAULT_FILTER_IMPL_FOLDER);
        assertThat(conf.getLibFolder()).isEqualTo(ActorFilter.DEFAULT_LIB_FOLDER);
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