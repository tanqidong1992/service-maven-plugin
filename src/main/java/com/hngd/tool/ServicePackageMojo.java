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
import org.codehaus.plexus.interpolation.os.OperatingSystemUtils;
import org.codehaus.plexus.util.FileUtils;

import com.hngd.tool.constant.ServiceTypes;
import com.hngd.tool.exception.ScriptGenerationException;
import com.hngd.tool.utils.BuildInfoUtils;
import com.hngd.tool.utils.JreUtils;
import com.hngd.tool.utils.MavenProjectUtils;

/**
 * 程序执行脚本生成MOJO
 * @author tqd
 *
 */
@Mojo(name = "service-package", defaultPhase = LifecyclePhase.VERIFY)
public class ServicePackageMojo extends AbstractMojo {
	/**
	 * pakcage service type, NT or Systemd,default value is NT
	 */
	@Parameter(required = false)
	public String serviceType;
	/**
	 * jre directory for copy
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
	 * resources to be copied to the package base directory
	 */
	@Parameter
	public List<File> resources;

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
		if(serviceType==null) {
			if(JreUtils.isLinux()) {
				serviceType=ServiceTypes.SYSTEMD;
			}else if(JreUtils.isWindows()){
				serviceType=ServiceTypes.NT;
			}
		}else {
			throw new MojoExecutionException("The parameter[serviceType] is empty!");
		}
		log.info("Start to package "+serviceType+" Service");
		String buildOutputPath = mavenProject.getBuild().getDirectory();
		String artifactId=mavenProject.getArtifactId();
		if(outputDirectory==null) {
			outputDirectory=new File(buildOutputPath, artifactId);
		}
		String targetJarFileName = MavenProjectUtils.generateJarFileName(mavenProject);
		String originalTargetJarFileName = targetJarFileName;
		log.debug("Target jar file name is " + targetJarFileName);
		if (MavenProjectUtils.isSpringBootPluginExist(mavenProject)) {
			log.info("The project is packaged as spring boot flat jar,we need to obtain the origin jar file!");
			targetJarFileName += ".original";
		}
		String targetMainJarFilePath = buildOutputPath + File.separator + targetJarFileName;
		if (!FileUtils.fileExists(targetMainJarFilePath)) {
			log.error("The target jar file is not found");
			throw new MojoExecutionException("The target jar file["+targetMainJarFilePath+"] is not found!"
					+ "You may need to execute mvn package first!");
		}
		log.info("Clean output directory:"+outputDirectory.getAbsolutePath());
		if (outputDirectory.exists()) {
			try {
				FileUtils.deleteDirectory(outputDirectory);
			} catch (IOException e) {
				log.error("",e);
				throw new MojoExecutionException("Clean the outputDirectory["+outputDirectory.getAbsolutePath()+"] failed!", e);
			}
		}
		outputDirectory.mkdirs();
		log.info("Copy dependent libraries");
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
			ScriptGeneratorContext.generateScripts(mavenProject,scriptConfigFile, outputDirectory, dependentLibDirectory, mainJarFile,serviceType);
		} catch (ScriptGenerationException e) {
			log.error("", e);
			throw new MojoExecutionException("生成安装脚本错误!",e);
		}

		log.info("Copy or custom java runtime image");
		File outputJreDirectory = new File(outputDirectory, "jre");
		outputJreDirectory.mkdirs();
		
		if(jreDirectory==null && customRuntimeImage && JreUtils.atLeastJava11()) {
			log.info("Start to custom java runtime image...");
			long startTime=System.currentTimeMillis();
			try {
				RuntimeImageCreator.build(mainJarFile,dependentLibDirectory,outputJreDirectory,targetJreVersion,compressLevel);
			} catch (Exception e) {
				log.error("", e);
				throw new MojoExecutionException("定制Jre失败!",e);
			}
			log.info("Custom java runtime image using time:"+(System.currentTimeMillis()-startTime)+"ms");
		}else {
			
			if (jreDirectory == null) {
				String defaultJrePath = JreUtils.getDefaultJrePath();
				log.info("The config item[jreDirectory] is empty, using default jre path:" + defaultJrePath);
				jreDirectory = new File(defaultJrePath);
			}
			
			try {
				FileUtils.copyDirectoryStructureIfModified(jreDirectory, outputJreDirectory);
			} catch (IOException e) {
				log.error("", e);
				throw new MojoExecutionException("复制Jre失败!",e);
			}
		}
		
		log.info("Start to generate build info");
		try {
			BuildInfoUtils.generateBuildInfo(mavenProject.getBasedir(), outputDirectory);
		} catch (IOException e) {
			throw new MojoExecutionException("Generate build info failed!", e);
		}
		
	}
 
	private void copyResourceDirectories() throws IOException {
		log.info("Start to copy resources");
		if(resources==null) {
			return;
		}
		for (File file : resources) {
			if (!file.exists()) {
				log.info("The resource [" + file.getAbsolutePath() + "] is not found,skiped it");
				continue;
			}
			String fileName = file.getName();
			File dst = new File(outputDirectory, fileName);
			if(file.isDirectory()) {
				dst.mkdirs();
				FileUtils.copyDirectoryStructureIfModified(file, dst);
			}else if(file.isFile()) {
				FileUtils.copyFile(file, dst);
			}
			
		}
	}

	private File copyDependentLibs() throws MojoExecutionException {
		log.debug("Start to copy dependent library files");
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
				log.error("Copy dependent library file:" + libFile.getAbsolutePath() + " failed!", e);
				throw new MojoExecutionException("Copy dependent library file:" + libFile.getAbsolutePath() + " failed!", e);
			}
		}
		return dependentLibDirectory;
	}
}
