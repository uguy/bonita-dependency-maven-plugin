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
import java.util.HashMap;
import java.util.List;

@Mojo(name = "connector-unpack",
        defaultPhase = LifecyclePhase.GENERATE_SOURCES,
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

    protected void moveToFolder(Path sourceFile, Path destinationFolder) {
        try {
            // Ensure destination folder exists
            File destFolder = destinationFolder.toFile();
            if (!destFolder.exists()) {
                destFolder.mkdirs();
            }

            // Copy file to destination
            Path destFile = destinationFolder.resolve(sourceFile.getFileName());
            getLog().info(String.format("copy file %s to %s", sourceFile, destFile));
            Files.copy(sourceFile, destFile, StandardCopyOption.REPLACE_EXISTING);

        } catch (IOException e) {
            throw new RuntimeException(
                    String.format("An error occured while copying file '%s' to '%s'", sourceFile, destinationFolder), e);
        }
    }

    protected Path unpackDependency(File artifactFile, Path workingDir) throws MojoExecutionException {
        try {
            if (!workingDir.toFile().exists()) {
                workingDir.toFile().mkdirs();
            }
            String targetFolderName = artifactFile.getName().replace(".zip", "");
            Path targetFolder = Files.createDirectory(workingDir.resolve(targetFolderName));
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

    protected org.eclipse.aether.artifact.DefaultArtifact toArtifact(Dependency dependency) {
        //"<groupId>:<artifactId>[:<extension>[:<classifier>]]:<version>"
        String coords = String.format("%s:%s:%s:%s:%s", dependency.getGroupId(), dependency.getArtifactId(), dependency.getType(), dependency.getClassifier(), dependency.getVersion());
//        DefaultArtifact defaultArtifact = new DefaultArtifact(coords);
        DefaultArtifact defaultArtifact = new DefaultArtifact(dependency.getGroupId(), dependency.getArtifactId(), dependency.getClassifier(), dependency.getType(), dependency.getVersion(), new HashMap<String, String>(), (File) null);
        return defaultArtifact;
    }
}
