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
public class NtServicePackageMojo extends AbstractMojo {
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
		log.debug("Start to package Windows NT Service");
		String buildOutputPath = mavenProject.getBuild().getDirectory();
		String artifactId=mavenProject.getArtifactId();
		if(outputDirectory==null) {
			outputDirectory=new File(buildOutputPath, artifactId);
		}
		
		String jarFileName = MavenProjectUtils.generateJarFileName(mavenProject);
		String originalJarFileName = jarFileName;
		log.debug("target jar file name is " + jarFileName);
		if (MavenProjectUtils.isSpringBootPluginExist(mavenProject)) {
			log.info("The project is packaged as spring boot flat jar,we need to obtain the origin jar file");
			jarFileName += ".original";
		}
		String jarFilePath = buildOutputPath + File.separator + jarFileName;
		boolean isJarFileExist = FileUtils.fileExists(jarFilePath);
		if (!isJarFileExist) {
			log.error("The target jar file is not found");
			throw new MojoExecutionException("The target jar file["+jarFilePath+"] is not found!,"
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
		log.info("copy dependent libs");
		File dependentLibDirectory = copyDependentLibs();
		log.debug("copy package jar file");
		File packageJarFile = new File(outputDirectory, originalJarFileName);
		try {
			FileUtils.copyFile(new File(jarFilePath), packageJarFile);
		} catch (IOException e) {
			log.error("", e);
			throw new MojoExecutionException("复制目标可执行Jar失败!",e);
		}
		log.info("copy resources");
		try {
			copyResourceDirectories();
		} catch (IOException e) {
			log.error("",e);
			throw new MojoExecutionException("复制资源目录失败!",e);
		}

		log.info("generate scripts");
		try {
			ScriptGenerator.generateScripts(mavenProject,scriptConfigFile, outputDirectory, dependentLibDirectory, packageJarFile);
		} catch (ScriptGenerationException e) {
			log.error("", e);
			throw new MojoExecutionException("生成安装脚本错误!",e);
		}

		log.info("copy jre");
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
		log.info("start to copy resources directories");
		if(resourceDirectories==null) {
			return;
		}
		for (File file : resourceDirectories) {
			if (!file.exists()) {
				log.info("the resource directory[" + file.getAbsolutePath() + "] is not found,skiped it");
				continue;
			}
			String directoryName = file.getName();
			File dstDirectory = new File(outputDirectory, directoryName);
			dstDirectory.mkdirs();
			FileUtils.copyDirectoryStructureIfModified(file, dstDirectory);
		}
	}

	private File copyDependentLibs() throws MojoExecutionException {
		log.debug("start to copy dependent lib files");
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
				log.error("Copy dependent Lib file:" + libFile.getAbsolutePath() + " failed!", e);
				throw new MojoExecutionException("Copy dependency file:" + libFile.getAbsolutePath() + " failed!", e);
			}
		}
		return dependentLibDirectory;
	}
}
