package org.bonitasoft.maven;

import lombok.Getter;
import lombok.Setter;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.repository.RemoteRepository;

import java.io.File;
import java.util.List;

@Getter
@Setter
public abstract class AbstractBonitaDependencyMojo extends AbstractMojo {

    @Parameter(readonly = true)
    protected MavenProject project;

    @Parameter(defaultValue = "${session}", readonly = true)
    protected MavenSession mavenSession;

    @Parameter(readonly = true)
    protected RepositorySystem repositorySystem;

    @Parameter(defaultValue = "${repositorySystemSession}", readonly = true)
    protected RepositorySystemSession repositorySystemSession;

    @Parameter(defaultValue = "${project.remotePluginRepositories}", readonly = true)
    protected List<RemoteRepository> remoteRepositories;

    @Parameter(property = "skip", defaultValue = "false")
    private boolean skip;

    @Parameter(defaultValue = "${project.basedir}")
    protected File projectDirectory;

    @Parameter(defaultValue = "${project.build.directory}")
    protected File buildDirectory;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        if (isSkip()) {
            getLog().info("Skipping plugin execution");
            return;
        }
        doExecute();
    }

    public abstract void doExecute() throws MojoExecutionException, MojoFailureException;


}
