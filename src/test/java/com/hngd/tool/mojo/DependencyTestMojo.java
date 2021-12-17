package com.hngd.tool.mojo;

import lombok.Data;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.ProjectDependenciesResolver;

import java.io.File;
import java.util.List;


@Mojo(name="dependency-test",defaultPhase = LifecyclePhase.PACKAGE)
@Data
public class DependencyTestMojo extends AbstractMojo {

    /**
     * jre directory to copy
     */
    @Parameter(required = false)
    public File jreDirectory;
    /**
     * if true and jreDirectory is null, use jlink to custome the java runtime image
     */
    @Parameter(required = false,defaultValue = "false")
    public Boolean customRuntimeImage;
    /**
     * if dependent libraries contains multi-release library,choose version:targetJreVersion
     */
    @Parameter(required = false,defaultValue = "11")
    public String targetJreVersion;
    /**
     * <0|1|2> Enable compression of resources
     * 0: No compression
     * 1: Constant string sharing
     * 2: ZIP
     */
    @Parameter(required = false,defaultValue = "2")
    public String compressLevel;
    @Parameter(defaultValue = "${project}", readonly = true, required = true)
    public MavenProject project;

    @Parameter(defaultValue = "${session}", readonly = true,required = true)
    private MavenSession session;
    // TODO: This is internal maven, we should find a better way to do this
    @Component
    private ProjectDependenciesResolver projectDependenciesResolver;

    @Parameter(defaultValue = "${reactorProjects}", required = true, readonly = true)
    private List<MavenProject> projects;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
    	/**
        List<File> dependentLibFiles=MavenProjectUtils.getDependentLibFiles(project,session,projectDependenciesResolver,projects);
        dependentLibFiles.forEach(f->{
            System.out.println(f.getAbsolutePath());
        });
        */
    }
}
