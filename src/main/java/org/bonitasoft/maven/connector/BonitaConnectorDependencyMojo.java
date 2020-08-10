package org.bonitasoft.maven.connector;

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
import org.bonitasoft.maven.util.ZipUtil;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.collection.CollectRequest;
import org.eclipse.aether.resolution.ArtifactResult;
import org.eclipse.aether.resolution.DependencyRequest;
import org.eclipse.aether.resolution.DependencyResolutionException;
import org.eclipse.aether.resolution.DependencyResult;
import org.eclipse.aether.util.filter.DependencyFilterUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;

@Mojo(name = "connector-unpack",
        defaultPhase = LifecyclePhase.GENERATE_RESOURCES,
        requiresDependencyResolution = ResolutionScope.RUNTIME,
        threadSafe = true)
public class BonitaConnectorDependencyMojo extends AbstractBonitaDependencyMojo {

    @Parameter
    private Connector connector;

    @Override
    public void doExecute() throws MojoExecutionException, MojoFailureException {
        DependencySelector dependencySelector = new DependencySelector(project.getDependencies(), connector.getIncludes());
        List<Dependency> dependencies = dependencySelector.select();
        for (Dependency dependency : dependencies) {
            File artifactFile = resolveDependencyFile(dependency);
            Path path = unpackDependency(artifactFile);
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

    private void moveToFolder(Path sourceFile, Path destinationFolder) {
        try {
            Files.copy(sourceFile, destinationFolder.resolve(sourceFile.getFileName()), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new RuntimeException(
                    String.format("An error occured while copying file '%s' to '%s'", sourceFile, destinationFolder), e);
        }
    }

    protected Path unpackDependency(File artifactFile) throws MojoExecutionException {
        try {
            if (!buildDirectory.exists()) {
                buildDirectory.mkdirs();
            }
            String targetFolderName = artifactFile.getName().replace(".zip", "");
            Path targetFolder = Files.createDirectory(buildDirectory.toPath().resolve(targetFolderName));
            ZipUtil.unzip(artifactFile, targetFolder);
            return targetFolder;
        } catch (IOException e) {
            throw new MojoExecutionException("Unable to unzip artifact.", e);
        }
    }

    protected File resolveDependencyFile(Dependency dependency) throws MojoExecutionException {
        Artifact artifact = this.getArtifactFile(dependency);
        // Now we have the artifact file locally stored available and we can do something with it
        return artifact.getFile();
    }

    protected Artifact getArtifactFile(Dependency dependency) throws MojoExecutionException {

        CollectRequest collectRequest = new CollectRequest();
        collectRequest.setRoot(new org.eclipse.aether.graph.Dependency(toArtifact(dependency), dependency.getScope()));
        remoteRepositories.forEach(collectRequest::addRepository);
        DependencyRequest dependencyRequest = new DependencyRequest(collectRequest, DependencyFilterUtils.classpathFilter(dependency.getScope()));

        DependencyResult dependencyResult;
        try {
            dependencyResult = repositorySystem.resolveDependencies(repositorySystemSession, dependencyRequest);
        } catch (DependencyResolutionException e) {
            throw new MojoExecutionException("Unable to find/resolve artifact: " + dependency.getManagementKey(), e);
        }

        ArtifactResult artifactResult = dependencyResult.getArtifactResults().stream().findFirst()
                .orElseThrow(() -> new MojoExecutionException("Unable to find/resolve artifact : " + dependency.getManagementKey()));

        return artifactResult.getArtifact();
    }

    private org.eclipse.aether.artifact.DefaultArtifact toArtifact(Dependency dependency) {
        return new DefaultArtifact(dependency.getManagementKey());
    }

    public void setConnector(Connector connector) {
        this.connector = connector;
    }
}
