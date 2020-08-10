package org.bonitasoft.maven.actorfilter;

import lombok.Getter;
import org.apache.commons.io.FilenameUtils;
import org.apache.maven.model.Dependency;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.bonitasoft.maven.AbstractBonitaDependencyMojo;
import org.bonitasoft.maven.DependencySelector;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

@Mojo(name = "actorfilter-unpack",
        defaultPhase = LifecyclePhase.PROCESS_RESOURCES,
        requiresDependencyResolution = ResolutionScope.RUNTIME,
        threadSafe = true)
public class BonitaActorFilterDependencyMojo extends AbstractBonitaDependencyMojo {

    @Getter
    @Parameter(name = "actorFilter", property = "actorFilter")
    private ActorFilter actorFilter = new ActorFilter();

    @Override
    public void doExecute() throws MojoExecutionException, MojoFailureException {
        DependencySelector dependencySelector = new DependencySelector(project.getDependencies(), actorFilter.getIncludes());
        List<Dependency> dependencies = dependencySelector.select();
        for (Dependency dependency : dependencies) {
            File artifactFile = resolveDependencyFile(dependency);
            Path path = unpackDependency(artifactFile, buildDirectory.toPath());
            try {
                Path projectPath = projectDirectory.toPath();
                Files.walk(path).forEach(child -> {
                    if (!child.toFile().isDirectory()) {
                        switch (FilenameUtils.getExtension(child.getFileName().toString())) {
                            case "def":
                            case "properties":
                            case "png": // TODO other image format?
                                moveToFolder(child, projectPath.resolve(actorFilter.getDefinitionFolder()));
                                break;
                            case "impl":
                                moveToFolder(child, projectPath.resolve(actorFilter.getImplementationFolder()));
                                break;
                            case "jar":
                                moveToFolder(child, projectPath.resolve(actorFilter.getLibFolder()));
                                break;
                            default:
                                getLog().warn(String.format("Unreconize extension for '%s', ignored.", child.getFileName()));
                                break;
                        }
                    }
                });
            } catch (IOException | RuntimeException e) {
                throw new MojoFailureException("An error occurred while moving files", e);
            }
        }
    }
}
