package com.hngd.tool.utils;

import java.io.File;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.Plugin;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.DefaultDependencyResolutionRequest;
import org.apache.maven.project.DependencyResolutionException;
import org.apache.maven.project.DependencyResolutionResult;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.ProjectDependenciesResolver;
import org.eclipse.aether.graph.Dependency;
import org.eclipse.aether.graph.DependencyFilter;

public class MavenProjectUtils {

	public static String generateJarFileName(MavenProject mavenProject) {
		String artifactId=mavenProject.getArtifactId();
		String version = mavenProject.getVersion();
		return artifactId + "-" + version + ".jar";
	}
	
	public static final String SPRING_BOOT_PLUGIN_ARTIFACT_ID = "spring-boot-maven-plugin";

	public static boolean isSpringBootPluginExist(MavenProject mavenProject) {
		List<Plugin> plugins = mavenProject.getBuildPlugins();
		if (plugins == null || plugins.size() <= 0) {
			return false;
		}
		return plugins.stream()
			.filter(plugin -> plugin.getArtifactId().equals(SPRING_BOOT_PLUGIN_ARTIFACT_ID))
			.findAny()
			.isPresent();
	}
	
	public static List<File> toDependentLibFiles(DependencyResolutionResult resolutionResult)
			throws MojoExecutionException {
	    List<File> files = resolutionResult.getDependencies().stream()
		    .map(Dependency::getArtifact)
			.map(org.eclipse.aether.artifact.Artifact::getFile)
		    .collect(Collectors.toList());
		return files;
		
	}
	
	
	public static DependencyResolutionResult resolveDependencies(MavenProject project, MavenSession session,
			ProjectDependenciesResolver projectDependenciesResolver, List<MavenProject> projects)
			throws MojoExecutionException {
		Set<String> projectArtifacts = projects.stream()
				.map(MavenProject::getArtifact)
				.map(Artifact::toString)
				.collect(Collectors.toSet());
		DependencyFilter ignoreProjectDependenciesFilter = (node, parents) -> {
			if (node == null || node.getDependency() == null) {
				// if nothing, then ignore
				return false;
			}
			if (projectArtifacts.contains(node.getArtifact().toString())) {
				// ignore project dependency artifacts
				return false;
			}
			Boolean optional=node.getDependency().getOptional();
			if(optional!=null && optional) {
				return false;
			}
			// we only want compile/runtime deps
			String scope=node.getDependency().getScope();
			return Artifact.SCOPE_COMPILE_PLUS_RUNTIME.contains(scope) || Artifact.SCOPE_SYSTEM.contains(scope);
		};
		 
		try {
			DependencyResolutionResult resolutionResult = projectDependenciesResolver
					.resolve(new DefaultDependencyResolutionRequest(project, session.getRepositorySession())
					.setResolutionFilter(ignoreProjectDependenciesFilter));
			
			return resolutionResult;
		} catch (DependencyResolutionException ex) {
			throw new MojoExecutionException("Failed to resolve dependencies", ex);
		}
	}
}
