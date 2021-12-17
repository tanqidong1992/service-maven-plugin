package com.hngd.tool.util;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeoutException;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.hngd.tool.exception.CustomJreImageException;

import org.apache.maven.shared.utils.StringUtils;
import org.codehaus.plexus.util.FileUtils;

import org.zeroturnaround.exec.InvalidExitValueException;
import org.zeroturnaround.exec.ProcessExecutor;
import org.zeroturnaround.exec.ProcessResult;
import org.zeroturnaround.exec.stream.slf4j.Slf4jStream;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RuntimeImageCreator {

    public static final String charsetName = Charset.defaultCharset().name();

    public static List<String> resolveJreDependencies(File mainJar, List<File> dependentJars, boolean isMultiReleaseJar,
            String jreVersion) {

        List<String> cmds = new ArrayList<>(jdeps);
        if (isMultiReleaseJar) {
            cmds.add("--multi-release");
            cmds.add(jreVersion);
        }
        if (dependentJars != null && !dependentJars.isEmpty()) {
            String classpath = dependentJars.stream()
                    .map(File::getAbsolutePath)
                    .collect(Collectors.joining(";"));
            cmds.add("-cp");
            cmds.add(classpath);
        }
        cmds.add(mainJar.getAbsolutePath());

        ProcessResult pr;
        try {
            pr = executeCmd(cmds);
        } catch (InvalidExitValueException | IOException | InterruptedException | TimeoutException e) {
            throw new CustomJreImageException("resolve Jre modules failed!",e);
        }
        String output=pr.outputString(charsetName);
        if(pr.getExitValue()!=0) {
            log.warn("resolveJreDependencies cmd:\n{}\n,result:{}",StringUtils.join(cmds.iterator(), " "),output);
            return Collections.emptyList();
        }else{
            log.info("resolveJreDependencies cmd:\n{}\n,result:{}",StringUtils.join(cmds.iterator(), " "),output);
            return resolveModules(output);
        }
    }
    public static final List<String> jdeps=Arrays.asList("jdeps","--print-module-deps","--ignore-missing-deps","-q");
    public static ProcessResult executeCmd(List<String> cmds) throws InvalidExitValueException, IOException, InterruptedException, TimeoutException {
        ProcessResult pr= new ProcessExecutor()
            .command(cmds)
            .readOutput(true)
            .redirectError(Slf4jStream.of(log).asDebug())
            .execute();
        return pr; 
    }
    
    public static List<String> resolveJreDependencies(File jarFile,String jreVersion) {
        return resolveJreDependencies(jarFile,Collections.emptyList(),isMultiReleaseJar(jarFile),jreVersion);
    }
    public static List<String> resolveModules(String output){
        
        if(StringUtils.isEmpty(output)) {
            return Collections.emptyList();
        }
        List<String> modules=new LinkedList<>();
        output=output.trim();
        if(output.contains(",")) {
            Arrays.stream(output.split(","))
                .map(String::trim)
                .filter(StringUtils::isNotBlank)
                .forEach(modules::add);
        }else {
            if(output.length()>0) {
                modules.add(output);
            }
        }
        return modules;
    }
    /**
     * <a href="http://openjdk.java.net/jeps/238>JEP 238: Multi-Release JAR Files</a>
     * @param jar
     * @return
     */
    public static boolean isMultiReleaseJar(File jar) {
        try (JarFile jarFile=new JarFile(jar)){
            Manifest manifest=jarFile.getManifest();
            if(manifest==null) {
                return false;
            }
            Attributes a=manifest.getMainAttributes();
            Optional<Object> optionalValue=a.keySet().stream()
              .filter(Attributes.Name.class::isInstance)
              .map(Attributes.Name.class::cast)
              .filter(attributeName->attributeName.toString().equals("Multi-Release"))
              .map(a::get)
              .findFirst();
            return optionalValue.isPresent() && "true".equals(optionalValue.get());
        } catch (IOException e) {
            log.error("",e);
        }
        return false;
    }
    
    public static void build(File mainJar, File dependentLibDirectory, File outputJreDirectory,String targetJreVersion,String compressLevel) throws InvalidExitValueException, IOException, InterruptedException, TimeoutException {
        if(outputJreDirectory.exists()) {
            FileUtils.deleteDirectory(outputJreDirectory);
        }
        File[] files=dependentLibDirectory.listFiles();
        List<File> dependentJars=Stream.of(files)
           .filter(f->f.getName().endsWith(".jar"))
           .collect(Collectors.toList());
        Set<String> jreModules=resolveJreModules(mainJar, dependentJars, targetJreVersion);
        String modulesStr=StringUtils.join(jreModules.iterator(), ",");
        List<String> cmds=Arrays.asList("jlink","--compress",compressLevel,"--output",outputJreDirectory.getAbsolutePath(),"--add-modules",modulesStr);
        log.info("Custom java runtime image cmd:\n{}\n",StringUtils.join(cmds.iterator(), " "));
        ProcessResult result = new ProcessExecutor()
            .command("jlink","--compress",compressLevel,"--output",outputJreDirectory.getAbsolutePath(),"--add-modules",modulesStr)
            .redirectError(Slf4jStream.of(log).asDebug())
            .readOutput(true)
            .execute();
        int exitValue=result.getExitValue();
        if(exitValue!=0) {
            String s=result.outputString(charsetName);
            throw new RuntimeException(s, null);
        }
    }

    protected static Set<String> resolveJreModules(File mainJar,List<File> dependentJars,String targetJreVersion){
        log.debug("Start analysis dependent jar jre modules");
        Set<String> jreModules=new HashSet<>();
        boolean multiReleaseJarContained= dependentJars.stream()
            .anyMatch(RuntimeImageCreator::isMultiReleaseJar);
        dependentJars.parallelStream().forEach(jarFile->{
            //尝试单独分析某一个jar,如果分析失败,将依赖jar传入cp参数再一次分析
            List<String> dependentJreModules=resolveJreDependencies(jarFile, targetJreVersion);
            if(dependentJreModules.size()<=0) {
                List<File> newList=new ArrayList<>();
                newList.addAll(dependentJars);
                newList.remove(jarFile);
                dependentJreModules=resolveJreDependencies(jarFile,newList,multiReleaseJarContained,targetJreVersion);
            }
            if(dependentJreModules.size()>0) {
                synchronized(jreModules){
                    jreModules.addAll(dependentJreModules);
                }
                
            }
        });
        log.debug("Start analysis main jar jre modules");
        List<String> mainJreModules=resolveJreDependencies(mainJar,dependentJars,multiReleaseJarContained,targetJreVersion);
        for(String module:mainJreModules) {
            jreModules.add(module);
        }
        //default 
        jreModules.add("jdk.charsets");
        return jreModules;
    }
}
