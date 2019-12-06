package com.hngd.tool;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.ProjectDependenciesResolver;
import org.codehaus.plexus.util.FileUtils;

import com.hngd.tool.exception.ScriptGenerationException;
import com.hngd.tool.utils.JreUtils;
import com.hngd.tool.utils.MavenProjectUtils;

/**
 * Window下程序执行脚本生成MOJO
 *
 */
@Mojo(name = "win-package", defaultPhase = LifecyclePhase.VERIFY)
public class NTServicePackageMojo extends AbstractMojo {
	/**
	 * jre directory for copy
	 */
	@Parameter(required = false)
	public File jreDirectory;
	/**
	 * config properties for script generation
	 */
	@Parameter(required = false)
	public File scriptConfigFile;
	/**
	 * the directory to save the package file,the default value is  ${project.build.directory}/${project.artifactId}
	 */
	@Parameter(required = false)
	public File outputDirectory;
	/**
	 * resource directories
	 */
	@Parameter
	public List<File> resourceDirectories;

	@Component
	public MavenProject mavenProject;
	
	@Parameter(defaultValue = "${session}", readonly = true)
	private MavenSession session;
	// TODO: This is internal maven, we should find a better way to do this
	@Component
	private ProjectDependenciesResolver projectDependenciesResolver;
	@Parameter(defaultValue = "${reactorProjects}", required = true, readonly = true)
	private List<MavenProject> projects;

	Log log;

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		log = getLog();
		log.info("Start to package Windows NT Service");
		String buildOutputPath = mavenProject.getBuild().getDirectory();
		String artifactId=mavenProject.getArtifactId();
		if(outputDirectory==null) {
			outputDirectory=new File(buildOutputPath, artifactId);
		}
		String targetJarFileName = MavenProjectUtils.generateJarFileName(mavenProject);
		String originalTargetJarFileName = targetJarFileName;
		log.debug("target jar file name is " + targetJarFileName);
		if (MavenProjectUtils.isSpringBootPluginExist(mavenProject)) {
			log.info("The project is packaged as spring boot flat jar,we need to obtain the origin jar file");
			targetJarFileName += ".original";
		}
		String targetMainJarFilePath = buildOutputPath + File.separator + targetJarFileName;
		if (!FileUtils.fileExists(targetMainJarFilePath)) {
			log.error("The target jar file is not found");
			throw new MojoExecutionException("The target jar file["+targetMainJarFilePath+"] is not found!,"
					+ "You may need to execute package first!");
		}
		log.info("clean output directory:"+outputDirectory.getAbsolutePath());
		if (outputDirectory.exists()) {
			try {
				FileUtils.deleteDirectory(outputDirectory);
			} catch (IOException e) {
				log.error("",e);
				throw new MojoExecutionException("clean the outputDirectory["+outputDirectory.getAbsolutePath()+"] failed!", e);
			}
		}
		outputDirectory.mkdirs();
		log.info("Copy dependent libs");
		File dependentLibDirectory = copyDependentLibs();
		log.info("Copy main jar file");
		File mainJarFile = new File(outputDirectory, originalTargetJarFileName);
		try {
			FileUtils.copyFile(new File(targetMainJarFilePath), mainJarFile);
		} catch (IOException e) {
			log.error("", e);
			throw new MojoExecutionException("复制目标可执行Jar失败!",e);
		}
		log.info("Copy resources");
		try {
			copyResourceDirectories();
		} catch (IOException e) {
			log.error("",e);
			throw new MojoExecutionException("复制资源目录失败!",e);
		}

		log.info("Generate scripts");
		try {
			ScriptGenerator.generateScripts(mavenProject,scriptConfigFile, outputDirectory, dependentLibDirectory, mainJarFile);
		} catch (ScriptGenerationException e) {
			log.error("", e);
			throw new MojoExecutionException("生成安装脚本错误!",e);
		}

		log.info("Copy jre");
		if (jreDirectory == null) {
			String defaultJrePath = JreUtils.getDefaultJrePath();
			log.info("jreDirectory is empty using default jre path:" + defaultJrePath);
			jreDirectory = new File(defaultJrePath);
		}
		File outputJreDirectory = new File(outputDirectory, "jre");
		outputJreDirectory.mkdirs();
		try {
			FileUtils.copyDirectoryStructureIfModified(jreDirectory, outputJreDirectory);
		} catch (IOException e) {
			log.error("", e);
			throw new MojoExecutionException("复制Jre失败!",e);
		}
	}
 
	private void copyResourceDirectories() throws IOException {
		log.info("Start to copy resources directories");
		if(resourceDirectories==null) {
			return;
		}
		for (File file : resourceDirectories) {
			if (!file.exists()) {
				log.info("The resource directory[" + file.getAbsolutePath() + "] is not found,skiped it");
				continue;
			}
			String directoryName = file.getName();
			File dstDirectory = new File(outputDirectory, directoryName);
			dstDirectory.mkdirs();
			FileUtils.copyDirectoryStructureIfModified(file, dstDirectory);
		}
	}

	private File copyDependentLibs() throws MojoExecutionException {
		log.debug("Start to copy dependent lib files");
		File dependentLibDirectory = new File(outputDirectory, "libs");
		dependentLibDirectory.mkdirs();
		List<File> dependentLibFiles=MavenProjectUtils.getDependentLibFiles(mavenProject, session, projectDependenciesResolver, projects);
		if(dependentLibFiles==null) {
			return dependentLibDirectory;
		}
		for (File libFile : dependentLibFiles) {
			try {
				FileUtils.copyFileToDirectory(libFile, dependentLibDirectory);
			} catch (IOException e) {
				log.error("Copy dependent lib file:" + libFile.getAbsolutePath() + " failed!", e);
				throw new MojoExecutionException("Copy dependency file:" + libFile.getAbsolutePath() + " failed!", e);
			}
		}
		return dependentLibDirectory;
	}
}
