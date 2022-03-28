package com.hngd.tool.mojo;

import java.io.File;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.FileSet;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.DefaultDependencyResolutionRequest;
import org.apache.maven.project.DependencyResolutionException;
import org.apache.maven.project.DependencyResolutionResult;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.ProjectDependenciesResolver;

import org.eclipse.aether.graph.Dependency;
import org.eclipse.aether.graph.DependencyFilter;

import com.google.common.collect.ImmutableSet;
/**
 * resolver for dependencies 
 * @author tqd
 *
 */
@Deprecated
@Mojo(name = "dependencies", defaultPhase = LifecyclePhase.PACKAGE)
public class ResolveDependencies extends AbstractMojo{

	@Component
	public MavenProject project;
	@Parameter(defaultValue = "${session}", readonly = true)
	private MavenSession session;
	// TODO: This is internal maven, we should find a better way to do this
	@Component
	private ProjectDependenciesResolver projectDependenciesResolver;
	@Parameter(defaultValue = "${reactorProjects}", required = true, readonly = true)
	private List<MavenProject> projects;
	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		
		  // print out pom configuration files
	    System.out.println(project.getFile());
	    if ("pom".equals(project.getPackaging())) {
	      // done if <packaging>pom</packaging>
	      return;
	    }

	    // print out sources directory (resolved by maven to be an absolute path)
	    System.out.println(project.getBuild().getSourceDirectory());

	    // print out resources directory (resolved by maven to be an absolute path)
	    ImmutableSet.copyOf(project.getBuild().getResources())
	        .stream()
	        .map(FileSet::getDirectory)
	        .forEach(System.out::println);
 

	    // Grab non-project SNAPSHOT dependencies for this project
	    // TODO: this whole sections relies on internal maven API, it could break. We need to explore
	    // TODO: better ways to resolve dependencies using the public maven API.
	    Set<String> projectArtifacts =
	        projects
	            .stream()
	            .map(MavenProject::getArtifact)
	            .map(Artifact::toString)
	            .collect(Collectors.toSet());

	    DependencyFilter ignoreProjectDependenciesFilter =
	        (node, parents) -> {
	          if (node == null || node.getDependency() == null) {
	            // if nothing, then ignore
	            return false;
	          }
	          if (projectArtifacts.contains(node.getArtifact().toString())) {
	            // ignore project dependency artifacts
	            return false;
	          }
	          // we only want compile/runtime deps
	          return Artifact.SCOPE_COMPILE_PLUS_RUNTIME.contains(node.getDependency().getScope());
	        };

	    try {
	      DependencyResolutionResult resolutionResult =
	          projectDependenciesResolver.resolve(
	              new DefaultDependencyResolutionRequest(project, session.getRepositorySession())
	                  .setResolutionFilter(ignoreProjectDependenciesFilter));
	      System.out.println("print snapshot dependencies");
	      resolutionResult
	          .getDependencies()
	          .stream()
	          .map(Dependency::getArtifact)
	          .filter(org.eclipse.aether.artifact.Artifact::isSnapshot)
	          .map(org.eclipse.aether.artifact.Artifact::getFile)
	          .forEach(System.out::println);
	      
	      System.out.println("runtime dependencies");
	      resolutionResult
              .getDependencies()
              .stream()
              .map(Dependency::getArtifact)
              //.filter(org.eclipse.aether.artifact.Artifact::isSnapshot)
              .map(org.eclipse.aether.artifact.Artifact::getFile)
              .forEach(System.out::println);
	    } catch (DependencyResolutionException ex) {
	      throw new MojoExecutionException("Failed to resolve dependencies", ex);
	    }
	}
	
	
	public static List<File> getDependencies(MavenProject project,MavenSession session,
	    ProjectDependenciesResolver projectDependenciesResolver,List<MavenProject> projects) 
					throws MojoExecutionException{
	    Set<String> projectArtifacts =projects
		            .stream()
		            .map(MavenProject::getArtifact)
		            .map(Artifact::toString)
		            .collect(Collectors.toSet());
		DependencyFilter ignoreProjectDependenciesFilter =
		        (node, parents) -> {
		          if (node == null || node.getDependency() == null) {
		            // if nothing, then ignore
		            return false;
		          }
		          if (projectArtifacts.contains(node.getArtifact().toString())) {
		            // ignore project dependency artifacts
		            return false;
		          }
		          // we only want compile/runtime deps
		          return Artifact.SCOPE_COMPILE_PLUS_RUNTIME.contains(node.getDependency().getScope());
		        };

		    try {
		      DependencyResolutionResult resolutionResult =
		          projectDependenciesResolver.resolve(
		              new DefaultDependencyResolutionRequest(project, session.getRepositorySession())
		                  .setResolutionFilter(ignoreProjectDependenciesFilter));
		     
		      List<File> files=resolutionResult
	              .getDependencies()
	              .stream()
	              .map(Dependency::getArtifact)
	              //.filter(org.eclipse.aether.artifact.Artifact::isSnapshot)
	              .map(org.eclipse.aether.artifact.Artifact::getFile)
	              .collect(Collectors.toList());
		      return files;
		    } catch (DependencyResolutionException ex) {
		      throw new MojoExecutionException("Failed to resolve dependencies", ex);
		    }
	}

}
