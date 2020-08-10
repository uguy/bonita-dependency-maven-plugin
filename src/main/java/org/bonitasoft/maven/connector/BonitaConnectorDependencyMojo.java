package org.bonitasoft.maven.connector;

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

@Mojo(name = "connector-unpack",
        defaultPhase = LifecyclePhase.PROCESS_RESOURCES,
        requiresDependencyResolution = ResolutionScope.RUNTIME,
        threadSafe = true)
public class BonitaConnectorDependencyMojo extends AbstractBonitaDependencyMojo {

    @Getter
    @Parameter(name = "connector", property = "connector")
    private Connector connector = new Connector();

    @Override
    public void doExecute() throws MojoExecutionException, MojoFailureException {
        DependencySelector dependencySelector = new DependencySelector(project.getDependencies(), connector.getIncludes());
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
                                moveToFolder(child, projectPath.resolve(connector.getDefinitionFolder()));
                                break;
                            case "impl":
                                moveToFolder(child, projectPath.resolve(connector.getImplementationFolder()));
                                break;
                            case "jar":
                                moveToFolder(child, projectPath.resolve(connector.getLibFolder()));
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
