package org.bonitasoft.maven.dependency.actorfilter;

import lombok.Getter;
import org.apache.commons.io.FilenameUtils;
import org.apache.maven.model.Dependency;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.bonitasoft.maven.dependency.AbstractBonitaDependencyMojo;
import org.bonitasoft.maven.dependency.DependencySelector;
import org.bonitasoft.maven.dependency.exception.BonitaMojoException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static java.lang.String.*;

/**
 * Actor filter Bonita Dependency Mojo. Manage Actor filter dependencies for a bonita project (unpack resources at the right place)
 */
@Mojo(name = "actorfilter-unpack",
        defaultPhase = LifecyclePhase.PROCESS_RESOURCES,
        requiresDependencyResolution = ResolutionScope.RUNTIME,
        threadSafe = true)
public class BonitaActorFilterDependencyMojo extends AbstractBonitaDependencyMojo {

    /**
     * The actor filter includes configuration
     */
    @Getter
    @Parameter(name = "actorFilter", property = "actorFilter")
    private ActorFilter actorFilter = new ActorFilter();

    @Override
    public void doExecute() throws MojoExecutionException {

        // Filter actor filter dependencies from project dependencies
        DependencySelector dependencySelector = new DependencySelector(project.getDependencies(), actorFilter.getIncludes());
        List<Dependency> dependencies = dependencySelector.select();

        // Unpack dependencies
        for (Dependency dependency : dependencies) {
            File artifactFile = resolveDependencyFile(dependency);
            Path path = unpackDependency(artifactFile, projectBuildDirectory.toPath());
            try {
                Path projectPath = projectBaseDirectory.toPath();
                Files.walk(path).forEach(child -> {
                    if (!child.toFile().isDirectory()) {
                        switch (FilenameUtils.getExtension(child.getFileName().toString())) {
                            case "def":
                            case "properties":
                            case "png":
                            case "jpg":
                            case "jpeg": // TODO other image format?
                                copyToFolder(child, projectPath.resolve(actorFilter.getDefinitionFolder()));
                                break;
                            case "impl":
                                copyToFolder(child, projectPath.resolve(actorFilter.getImplementationFolder()));
                                break;
                            case "jar":
                                copyToFolder(child, projectPath.resolve(actorFilter.getLibFolder()));
                                break;
                            default:
                                getLog().warn(format("Unknown extension for '%s', will be ignored.", child.getFileName()));
                                break;
                        }
                    }
                });
            } catch (IOException e) {
                throw new BonitaMojoException("An error occurred while moving files", e);
            }
        }
    }
}
