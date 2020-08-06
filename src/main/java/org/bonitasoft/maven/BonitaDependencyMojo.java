package org.bonitasoft.maven;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.FilenameUtils;

/*
 * Copyright 2001-2005 The Apache Software Foundation.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.Dependency;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;
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

@Mojo(name = "unpack",
        defaultPhase = LifecyclePhase.GENERATE_RESOURCES,
        requiresDependencyResolution = ResolutionScope.RUNTIME,
        requiresProject = true,
        threadSafe = true)
public class BonitaDependencyMojo extends AbstractMojo {

    /**
     * POM
     */
    @Component
    private MavenProject project;

    /**
     * The Maven session
     */
    @Component
    protected MavenSession mavenSession;

    @Component
    private RepositorySystem repositorySystem;

    @Parameter(defaultValue = "${repositorySystemSession}", readonly = true)
    private RepositorySystemSession repositorySystemSession;

    @Parameter(defaultValue = "${project.remotePluginRepositories}", readonly = true)
    private List<RemoteRepository> remoteRepositories;

    /**
     * Skip plugin execution completely.
     */
    @Parameter(property = "skip", defaultValue = "false")
    private boolean skip;

    @Parameter
    private Set<String> includes;

    @Parameter(defaultValue = "${project.basedir}")
    private File projectDirectory;

    @Parameter(defaultValue = "${project.build.directory}")
    private File buildDirectory;

    private static final String CONNECTOR_DEF_FOLDER = "connectors-def";
    private static final String CONNECTOR_IMPL_FOLDER = "connectors-impl";
    private static final String LIB_FOLDER = "lib";

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        if (isSkip()) {
            getLog().info("Skipping plugin execution");
            return;
        }
        ArtifactSelector artifactSelector = new ArtifactSelector(project.getDependencies(), includes);

        List<Dependency> dependencies = artifactSelector.getDependencies();
        for (Dependency dependency : dependencies) {
            Path path = unpackDependency(dependency);
            try {
                Path projectPath = projectDirectory.toPath();
                Files.walk(path).forEach(child -> {
                    if (!child.toFile().isDirectory()) {
                        switch (FilenameUtils.getExtension(child.getFileName().toString())) {
                            case "def":
                            case "properties":
                            case "png": // TODO other image format?
                                moveToFolder(child, projectPath.resolve(CONNECTOR_DEF_FOLDER));
                                break;
                            case "impl":
                                moveToFolder(child, projectPath.resolve(CONNECTOR_IMPL_FOLDER));
                                break;
                            case "jar":
                                moveToFolder(child, projectPath.resolve(LIB_FOLDER));
                                break;
                            default:
                                getLog().warn(String.format("Unreconize extension for '%s', ignored.",
                                        child.getFileName().toString()));
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

    protected Path unpackDependency(Dependency dependency) throws MojoExecutionException {
        try {
            File artifactFile = this.getArtifactFile(dependency);
            if (!buildDirectory.exists()) {
                buildDirectory.mkdirs();
            }
            Path targetDir = Files.createDirectory(buildDirectory.toPath().resolve(dependency.getArtifactId()));
            ZipUtil.unzip(artifactFile, targetDir);
            return targetDir;
        } catch (IOException e) {
            throw new MojoExecutionException("Unable to unzip artifact.", e);
        }
    }

    protected File getArtifactFile(Dependency dependency) throws MojoExecutionException {
        CollectRequest collectRequest = new CollectRequest();
        collectRequest.setRoot(new org.eclipse.aether.graph.Dependency(toArtifact(dependency), dependency.getScope()));
        remoteRepositories.forEach(collectRequest::addRepository);
        DependencyRequest dependencyRequest = new DependencyRequest(collectRequest,
                DependencyFilterUtils.classpathFilter(dependency.getScope()));

        DependencyResult dependencyResult;
        try {
            dependencyResult = repositorySystem.resolveDependencies(repositorySystemSession, dependencyRequest);
        } catch (DependencyResolutionException e) {
            throw new MojoExecutionException("Unable to find/resolve artifact.", e);
        }

        ArtifactResult artifactResult = dependencyResult.getArtifactResults().stream().findFirst()
                .orElseThrow(() -> new MojoExecutionException("Unable to find/resolve artifact."));
        Artifact resolvedArtifact = artifactResult.getArtifact();
        // Now we have the artifact file locally stored available and we can do something with it
        return resolvedArtifact.getFile();
    }

    private org.eclipse.aether.artifact.DefaultArtifact toArtifact(Dependency dependency) {
        String groupId = dependency.getGroupId();
        String artifactId = dependency.getArtifactId();
        String version = dependency.getVersion();
        String type = dependency.getType();
        String classifier = dependency.getClassifier();
        String coord = String.format("%s:%s:%s:%s", groupId, artifactId, type, version); // TODO check it works
        return new DefaultArtifact(coord);
    }

    /**
     * @return {@link #skip}
     */
    public boolean isSkip() {
        return skip;
    }
}
