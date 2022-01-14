package com.hngd.tool;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.hngd.tool.config.ConfigItems;
import com.hngd.tool.generator.ScriptGeneratorContext;
import com.hngd.tool.util.*;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.DependencyResolutionResult;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.ProjectDependenciesResolver;
import org.codehaus.plexus.util.FileUtils;

import com.hngd.tool.MojoParameters.ResourceDirectoryParameter;
import com.hngd.tool.constant.ServiceTypes;
import com.hngd.tool.exception.ScriptGenerationException;

/**
 * Service Scripts Generator MOJO
 * @author tqd
 *
 */
@Mojo(name = "service-package", defaultPhase = LifecyclePhase.PACKAGE)
public class ServicePackageMojo extends AbstractMojo {
    /**
     * Package service type, Windows or Systemd,default value depends on the build runtime platform,In Windows
     * the value is Windows,In Linux,the value is Systemd
     */
    @Parameter(required = false)
    public String serviceType;
    /**
     * The jre directory for copy
     */
    @Parameter(required = false)
    public File jreDirectory;
    /**
     * If true and jreDirectory is null, use jlink to custom the java runtime image
     */
    @Parameter(required = false,defaultValue = "false")
    public Boolean customRuntimeImage;
    /**
     * if dependent libraries contains multi-release library,choose version:targetJreVersion
     * default value is 11
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
     * config properties file for script generation
     */
    @Parameter(required = false)
    public File scriptConfigFile;
    /**
     * the directory to save the package file,the default value is  ${project.build.directory}/${project.artifactId}
     */
    @Parameter(required = false)
    public File outputDirectory;
    /**
     * extra resources needed to be copied to the package base directory
     */
    @Parameter
    public List<ResourceDirectoryParameter> resources;

    @Component
    public MavenProject mavenProject;
    
    @Parameter(defaultValue = "${session}", readonly = true)
    private MavenSession session;
    // TODO: This is internal maven, we should find a better way to do this
    @Component
    private ProjectDependenciesResolver projectDependenciesResolver;
    
    @Parameter(defaultValue = "${reactorProjects}", required = true, readonly = true)
    private List<MavenProject> projects;
    /**
     * if true,output compressed file
     */
    @Parameter(required = false,defaultValue = "false")
    public Boolean outputZip;
    /**
     * if true,output RPM package spec file
     */
    @Parameter(required = false,defaultValue = "false")
    public Boolean outputRpmSpec;

    /**
     * Systemd Unit file WantedBy,default value is "multi-user.target"
     */
    @Parameter(required = false,defaultValue = "multi-user.target")
    public String wantedBy;
    /**
     * Systemd Unit file After,default value is "network.target"
     */
    @Parameter(required = false,defaultValue = "network.target")
    public String after;

    /**
     * if true,bind the JRE,default value is true
     */
    @Parameter(required = false,defaultValue = "true")
    public Boolean withJre;

    /**
     * The Environments for Systemd
     */
    @Parameter(required = false)
    private List<MojoParameters.Environment> environments;
    /**
     * additional flags to pass into the JVM when running your application
     */
    @Parameter(required = false)
    private List<String> jvmFlags;
    /**
     * additional program arguments appended to the command to start your application
     */
    @Parameter(required = false)
    private List<String> args;
    /**
     * 	the main class to launch the application from.
     */
    @Parameter(required = false)
    private String mainClass;
    /**
     * systemd restart
     */
    @Parameter(required = false,defaultValue = "always")
    public String restart;

    Log log;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        log = getLog();
        if(serviceType==null) {
            if(JreUtils.isLinux()) {
                serviceType=ServiceTypes.SYSTEMD;
            }else if(JreUtils.isWindows()){
                serviceType=ServiceTypes.WINDOWS;
            }
        }else if(!ServiceTypes.WINDOWS.equals(serviceType) && !ServiceTypes.SYSTEMD.equals(serviceType)){
            throw new MojoExecutionException("Unsupported service type:"+serviceType+"!");
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
            log.debug("The project is packaged as spring boot fat jar,we need to obtain the origin jar file!");
            targetJarFileName += ".original";
            //新版本的Spring Boot Maven Plugin不会改变原始jar的文件名，而是把fat jar命名为${原始jar名称}-exec
            String targetMainJarFilePath = buildOutputPath + File.separator + targetJarFileName;
            if (!FileUtils.fileExists(targetMainJarFilePath)) {
                targetJarFileName=originalTargetJarFileName;
            }
        }
        String targetMainJarFilePath = buildOutputPath + File.separator + targetJarFileName;
        if (!FileUtils.fileExists(targetMainJarFilePath)) {
            log.error("The target jar file is not found");
            throw new MojoExecutionException("The target jar file["+targetMainJarFilePath+"] is not found!"
                    + "You may need to execute mvn package first!");
        }
        log.info("Prepare output directory:"+outputDirectory.getAbsolutePath());
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
        DependencyResolutionResult dependencyResolutionResult=
                MavenProjectUtils.resolveDependencies(mavenProject, session, projectDependenciesResolver, projects);
        File dependentLibDirectory = copyDependentLibs(dependencyResolutionResult);
        log.info("Copy main jar file");
        File mainJarFile = new File(outputDirectory, originalTargetJarFileName);
        log.debug("Copy main jar file: "+targetMainJarFilePath+" --> "+mainJarFile.getAbsolutePath());
        try {
            FileUtils.copyFile(new File(targetMainJarFilePath), mainJarFile);
        } catch (IOException e) {
            log.error("", e);
            throw new MojoExecutionException("Copy java runtime failed!",e);
        }
        log.info("Copy resources");
        try {
            copyResourceDirectories();
        } catch (IOException e) {
            log.error("",e);
            throw new MojoExecutionException("Copy resources failed!",e);
        }
        if(System.getProperties().containsKey("withJre")){
            String withJreStr=System.getProperty("withJre");
            withJre=Boolean.valueOf(withJreStr);
        }
        Map<String,String> mavenParameterContext=buildMavenParameterContext();
        log.info("Generate scripts");
        try {
            ScriptGeneratorContext.generateScripts(mavenProject,
                    scriptConfigFile,
                    outputDirectory,
                    dependentLibDirectory,
                    mainJarFile,
                    serviceType,
                    outputRpmSpec,
                    mavenParameterContext);
        } catch (ScriptGenerationException e) {
            log.error("", e);
            throw new MojoExecutionException("Generate scripts failed!",e);
        }

        log.debug("Copy or custom java runtime image");
        if(withJre) {
            bindJre(mainJarFile,dependentLibDirectory);
        }
        log.info("Generate build info");
        try {
            BuildInfoUtils.generateBuildInfo(mavenProject.getBasedir(), outputDirectory);
        } catch (IOException e) {
            throw new MojoExecutionException("Generate build info failed!", e);
        }
        String archiveType;
        if(ServiceTypes.SYSTEMD.equals(serviceType)) {
            archiveType="tar.gz";
        }else {
            archiveType="zip";
        }
        File archiveFile=new File(mavenProject.getBuild().getDirectory(),
                mavenProject.getArtifactId()+"."+archiveType);
        
        if(outputZip) {
            log.info("Package the output archive");
            CompressUtils.compress(archiveFile, outputDirectory);
        }
    }

    private Map<String, String> buildMavenParameterContext() {
        Map<String,String> context=new HashMap<>();
        context.put(ConfigItems.SYSTEMD_UNIT_AFTER,after);
        context.put(ConfigItems.SYSTEMD_UNIT_WANTED_BY,wantedBy);
        if(withJre){
            context.put(ConfigItems.KEY_WITH_JRE,Boolean.TRUE.toString());
        }
        if(environments!=null && environments.size()>0){
            String envStr=SystemdUtils.buildEnvironment(environments);
            if(envStr!=null && envStr.length()>0) {
                context.put(ConfigItems.SYSTEMD_UNIT_ENVIRONMENT, envStr);
            }
        }
        if(args!=null && args.size()>0){
            String argsStr=args.stream().collect(Collectors.joining(" "));
            context.put(ConfigItems.KEY_ARGS,argsStr);
        }
        if(mainClass!=null){
            context.put(ConfigItems.KEY_MAIN_CLASS,mainClass);
        }
        if(jvmFlags!=null && jvmFlags.size()>0){
            String jvmFlagsStr=jvmFlags.stream().collect(Collectors.joining(" "));
            context.put(ConfigItems.KEY_JVM_FLAGS,jvmFlagsStr);
        }
        if(restart!=null){
            context.put(ConfigItems.SYSTEMD_UNIT_RESTART,restart);
        }
        return context;

    }

    private void bindJre(File mainJarFile,File dependentLibDirectory) throws MojoExecutionException {
        if(System.getProperties().containsKey("jreDirectory")){
            jreDirectory=new File(System.getProperty("jreDirectory"));
        }
        File outputJreDirectory = new File(outputDirectory, "jre");
        outputJreDirectory.mkdirs();
        if(jreDirectory==null && customRuntimeImage && JreUtils.atLeastJava11()) {
            log.info("Make a custom java runtime image");
            long startTime=System.currentTimeMillis();
            try {
                RuntimeImageCreator.build(mainJarFile,dependentLibDirectory,outputJreDirectory,targetJreVersion,compressLevel);
            } catch (Exception e) {
                log.error("", e);
                throw new MojoExecutionException("Failed to make a custom java runtime image!",e);
            }
            log.info("Make custom java runtime image using time:"+(System.currentTimeMillis()-startTime)+"ms");
        }else {
            log.info("Copy java runtime image");
            if (jreDirectory == null) {
                String defaultJrePath = JreUtils.getDefaultJrePath();
                log.debug("The config item[jreDirectory] is empty, using default jre path:" + defaultJrePath);
                jreDirectory = new File(defaultJrePath);
            }
            try {
                FileUtils.copyDirectoryStructureIfModified(jreDirectory, outputJreDirectory);
            } catch (IOException e) {
                log.error("", e);
                throw new MojoExecutionException("Copy Jre Directory["+
                        jreDirectory.getAbsolutePath()
                        +"] failed!",e);
            }
        }
    }
 
    private void copyResourceDirectories() throws IOException {
        log.debug("Start to copy resources");
        if(resources==null) {
            return;
        }
        for (ResourceDirectoryParameter resource : resources) {
            File from=resource.getFrom();
            if (!from.exists()) {
                log.info("The resource [" + from.getAbsolutePath() + "] is not found,skipped it");
                continue;
            }
            String fileName = resource.getInto();
            File dst = new File(outputDirectory, fileName);
            if(from.isDirectory()) {
                dst.mkdirs();
                FileUtils.copyDirectoryStructureIfModified(from, dst);
            }else if(from.isFile()) {
                FileUtils.copyFile(from, dst);
            }
            log.debug("copy resource: "+from.getAbsolutePath()+" --> "+dst.getAbsolutePath());
            
        }
    }

    private File copyDependentLibs(DependencyResolutionResult dependencyResolutionResult) throws MojoExecutionException {
        log.debug("Start to copy dependent library files");
        File dependentLibDirectory = new File(outputDirectory, "libs");
        dependentLibDirectory.mkdirs();
        List<File> dependentLibFiles=MavenProjectUtils.toDependentLibFiles(dependencyResolutionResult);
        if(dependentLibFiles==null) {
            return dependentLibDirectory;
        }
        for (File libFile : dependentLibFiles) {
            try {
                FileUtils.copyFileToDirectory(libFile, dependentLibDirectory);
                log.debug("Copy dependency: "+libFile.getAbsolutePath()+" --> "+dependentLibDirectory.getAbsolutePath());
            } catch (IOException e) {
                log.error("Copy dependent library file:" + libFile.getAbsolutePath() + " failed!", e);
                throw new MojoExecutionException("Copy dependent library file:" + libFile.getAbsolutePath() + " failed!", e);
            }
        }
        return dependentLibDirectory;
    }
}