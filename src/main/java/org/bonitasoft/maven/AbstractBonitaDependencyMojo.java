package org.bonitasoft.maven;

import lombok.Getter;
import lombok.Setter;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.Dependency;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.bonitasoft.maven.util.ZipUtil;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.collection.CollectRequest;
import org.eclipse.aether.repository.RemoteRepository;
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

@Getter
@Setter
public abstract class AbstractBonitaDependencyMojo extends AbstractMojo {

    @Parameter(defaultValue = "${project}", required = true, readonly = true)
    protected MavenProject project;
    @Parameter(defaultValue = "${session}", readonly = true)
    protected MavenSession mavenSession;

    @Parameter(defaultValue = "${repositorySystemSession}", readonly = true)
    protected RepositorySystemSession repositorySystemSession;
    @Component
    protected RepositorySystem repositorySystem;
    @Parameter(defaultValue = "${project.remoteProjectRepositories}", readonly = true)
    protected List<RemoteRepository> remoteRepositories;

    @Parameter(defaultValue = "${project.basedir}")
    protected File projectDirectory;
    @Parameter(defaultValue = "${project.build.directory}")
    protected File buildDirectory;

    @Parameter(property = "skip", defaultValue = "false")
    private boolean skip;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        if (isSkip()) {
            getLog().info("Skipping plugin execution");
            return;
        }
        doExecute();
    }

    public abstract void doExecute() throws MojoExecutionException, MojoFailureException;

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

    protected void moveToFolder(Path sourceFile, Path destinationFolder) {
        try {
            // Ensure destination folder exists
            File destFolder = destinationFolder.toFile();
            if (!destFolder.exists()) {
                destFolder.mkdirs();
            }

            // Copy file to destination
            Path destFile = destinationFolder.resolve(sourceFile.getFileName());
            getLog().debug(String.format("copy file %s to %s", sourceFile, destFile));
            Files.copy(sourceFile, destFile, StandardCopyOption.REPLACE_EXISTING);

        } catch (IOException e) {
            throw new RuntimeException(
                    String.format("An error occured while copying file '%s' to '%s'", sourceFile, destinationFolder), e);
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

    protected DefaultArtifact toArtifact(Dependency dependency) {
        //"<groupId>:<artifactId>[:<extension>[:<classifier>]]:<version>"
        String coords = String.format("%s:%s:%s:%s:%s", dependency.getGroupId(), dependency.getArtifactId(), dependency.getType(), dependency.getClassifier(), dependency.getVersion());
//        DefaultArtifact defaultArtifact = new DefaultArtifact(coords);
        DefaultArtifact defaultArtifact = new DefaultArtifact(dependency.getGroupId(), dependency.getArtifactId(), dependency.getClassifier(), dependency.getType(), dependency.getVersion(), new HashMap<String, String>(), (File) null);
        return defaultArtifact;
    }
}
