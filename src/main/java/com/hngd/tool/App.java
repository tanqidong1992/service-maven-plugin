package com.hngd.tool;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.maven.model.Plugin;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.FileUtils;

import com.hngd.tool.utils.JreUtils;

/**
 * Hello world!
 *
 */
@Mojo(name = "win-package", defaultPhase = LifecyclePhase.PACKAGE)
public class App extends AbstractMojo {
	/**
	 * jre directory for copy
	 */
	@Parameter(required = false)
	public File jreDirectory;

	/**
	 * config properties for  script generation
	 */
	@Parameter(required = true)
	public File scriptConfigFile;
	/**
	 * the directory to save the package file
	 */
	@Parameter(required = true)
	public File outputDirectory;
	/**
	 * the directory contains dependent libraries
	 */
	@Parameter(required = true)
	public File dependencyDirectory;
	/**
	 * config and data directories
	 */
	@Parameter
	public List<File> configAndDataDirectories;

	@Component
	public MavenProject mavenProject;

	Log log;

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		log = getLog();
		log.info("start to pakcage win");
		String buildOutputPath = mavenProject.getBuild().getDirectory();
		List<Plugin> plugins = mavenProject.getBuildPlugins();
		String jarFileName = generateJarFileName();
		String originalJarFileName = jarFileName;
		log.info("target jar file name is " + jarFileName);
		if (isExistSpringBootPlugin(plugins)) {
			log.info("the project is package as spring boot flat jar,we need to obtain the origin jar file");
			jarFileName += ".original";
		}
		//String dependenciesDirectory = resolveDependenciesDirectory(plugins);

		String jarFilePath = buildOutputPath + File.separator + jarFileName;

		boolean isJarFileExist = FileUtils.fileExists(jarFilePath);
		if (!isJarFileExist) {
			log.error("the target jar file is not found");
		}
		if (outputDirectory.exists()) {
			try {
				FileUtils.deleteDirectory(outputDirectory);
			} catch (IOException e) {
				log.error("",e);
			}

		}
		outputDirectory.mkdirs();
		File outDependencyDirectory = null;
		try {
			outDependencyDirectory = copyDependencies();
		} catch (IOException e) {
			log.error("", e);
		}
		log.info("copy package jar file");
		File packageJarFile = new File(outputDirectory, originalJarFileName);
		try {
			FileUtils.copyFile(new File(jarFilePath), packageJarFile);
		} catch (IOException e) {
			log.error("", e);
		}
		log.info("copy file to output:" + outputDirectory.getAbsolutePath());
		copyConfigAndDataDirectories();

		log.info("generate scripts");
		try {
			ScriptGenerator.generateScripts(scriptConfigFile, outputDirectory, outDependencyDirectory, packageJarFile);
		} catch (IOException e) {
			log.error("", e);
		}

		log.info("copy jre");
		if (jreDirectory == null) {
			String defaultJrePath = JreUtils.getDefaultJrePath();
			log.info("jreDirectory is empty using default jre path:" + defaultJrePath);
			jreDirectory = new File(defaultJrePath);
		}
		File outputJreDirectory = new File(outputDirectory, "jre");
		if (outputJreDirectory.exists()) {
			outputJreDirectory.delete();
		}
		outputJreDirectory.mkdirs();
		try {
			FileUtils.copyDirectoryStructureIfModified(jreDirectory, outputJreDirectory);
		} catch (IOException e) {
			log.error("", e);
		}
	}

	public static final String MAVEN_DEPENDENCY_PLUGIN_ARTIFACT_ID = "maven-dependency-plugin";

	private String resolveDependenciesDirectory(List<Plugin> plugins) {
		Plugin copyPlugin = plugins.stream().filter(plugin -> {
			return plugin.getArtifactId().equals(MAVEN_DEPENDENCY_PLUGIN_ARTIFACT_ID);
		}).findAny().get();
		Object config = copyPlugin.getExecutions().get(0).getConfiguration();
		log.info(config.toString());
		return null;
	}

	private void copyConfigAndDataDirectories() {
		log.info("start to copy config and data directories");
		if (configAndDataDirectories != null) {
			for (File file : configAndDataDirectories) {
				if (!file.exists()) {
					log.info("the file[" + file.getAbsolutePath() + "] is not found,skiped it");
					continue;
				}
				String directoryName = file.getName();
				File targetDirectory = new File(outputDirectory, directoryName);
				if (targetDirectory.exists()) {
					try {
						FileUtils.deleteDirectory(targetDirectory);
					} catch (IOException e) {
						log.error("", e);
					}
				}
				targetDirectory.mkdirs();
				try {
					FileUtils.copyDirectoryStructureIfModified(file, targetDirectory);
				} catch (IOException e) {
					log.error("", e);
				}
			}
		}

	}

	private File copyDependencies() throws IOException {
		log.info("start to copy dependencies");
		File outDependencyDirectory = new File(outputDirectory, "libs");
		if (outDependencyDirectory.exists()) {
			FileUtils.deleteDirectory(outDependencyDirectory);
		}
		outDependencyDirectory.mkdirs();
		FileUtils.copyDirectoryStructure(dependencyDirectory, outDependencyDirectory);

		return outDependencyDirectory;
	}

	private String generateJarFileName() {
		String name = mavenProject.getName();
		String version = mavenProject.getVersion();
		return name + "-" + version + ".jar";
	}

	public static final String SPRING_BOOT_PLUGIN_ARTIFACT_ID = "spring-boot-maven-plugin";

	private boolean isExistSpringBootPlugin(List<Plugin> plugins) {
		if (plugins == null || plugins.size() <= 0) {
			return false;
		}
		return plugins.stream()
				.filter(plugin -> plugin.getArtifactId().equals(SPRING_BOOT_PLUGIN_ARTIFACT_ID))
				.findAny()
				.isPresent();
	}

}
