package com.hngd.tool;

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

import org.apache.commons.lang3.StringUtils;
import org.codehaus.plexus.util.FileUtils;
import org.zeroturnaround.exec.InvalidExitValueException;
import org.zeroturnaround.exec.ProcessExecutor;
import org.zeroturnaround.exec.ProcessResult;
import org.zeroturnaround.exec.stream.slf4j.Slf4jStream;

import lombok.extern.slf4j.Slf4j;
@Slf4j
public class RuntimeImageCreator {

	public static final String charsetName=Charset.defaultCharset().name();
	public static List<String> resolveJdkInternals(File mainJar,List<File> libsJars,boolean isMultiReleaseJar,String jreVersion) throws InvalidExitValueException, IOException, InterruptedException, TimeoutException{
		List<String> jarFilePaths=libsJars.stream()
		    .map(f->f.getAbsolutePath())
		    .collect(Collectors.toList());
		String classpath=StringUtils.join(jarFilePaths, ";");
		List<String> cmds=new ArrayList<>(jdeps);
		if(isMultiReleaseJar) {
			cmds.addAll(Arrays.asList("--multi-release",jreVersion,"-cp",classpath,libsJars.get(0).getAbsolutePath()));
		}else {
			cmds.addAll(Arrays.asList("-cp",classpath,mainJar.getAbsolutePath()));
		}
		ProcessResult pr=executeCmd(cmds);
		String output=pr.outputString(charsetName);
		return resolveModules(output);
		
	}
	public static final List<String> jdeps=Arrays.asList("jdeps","--print-module-deps","-q");
	public static ProcessResult executeCmd(List<String> cmds) throws InvalidExitValueException, IOException, InterruptedException, TimeoutException {
		ProcessResult pr= new ProcessExecutor()
				.command(cmds)
				.readOutput(true)
				.redirectError(Slf4jStream.of(log).asDebug())
                .execute();
		return pr; 
		
	}
	//"--multi-release","11",
	public static List<String> resolveJdkInternals(File jarFile,String jreVersion) throws InvalidExitValueException, IOException, InterruptedException, TimeoutException{
		String output=null;
		List<String> cmds=new ArrayList<>(jdeps);
		if(isMultiReleaseJar(jarFile)) {
			cmds.addAll(Arrays.asList("--multi-release",jreVersion,jarFile.getAbsolutePath()));
		}else {
			cmds.addAll(Arrays.asList(jarFile.getAbsolutePath()));
		}
		ProcessResult pr=executeCmd(cmds);
		if(pr.getExitValue()!=0) {
			 return Collections.emptyList();
		 }
		output=pr.outputString(charsetName);
		return resolveModules(output);
		
	}
	public static List<String> resolveModules(String output){
		List<String> modules=new LinkedList<>();
		if(StringUtils.isNoneBlank(output)) {
			output=output.trim();
			if(output.contains(",")) {
				String[] items=output.split(",");
				for(String item:items) {
					item=item.trim();
					if(StringUtils.isNoneBlank(item)) {
						modules.add(item);
					}
				}
			}else {
				modules.add(output);
			}
		}
		return modules;
	}
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
		List<File> multiReleaseJars=new LinkedList<>();
		List<File> normalJars=new LinkedList<>();
		for(File f:files) {
			if(!f.getName().endsWith(".jar")) {
				continue;
			}
			if(isMultiReleaseJar(f)) {
				multiReleaseJars.add(f);
			}else {
				normalJars.add(f);
			}
		}
		
		Set<String> modules=new HashSet<>();
		for(File f:files) {
			if(!f.getName().endsWith(".jar")) {
				continue;
			}
			//尝试单独分析某一个jar,如果分析失败,将依赖jar传入cp参数再一次分析
			List<String> dependentModules=resolveJdkInternals(f, targetJreVersion);
			if(dependentModules.size()>0) {
				
			}else if(multiReleaseJars.contains(f)) {
				dependentModules=resolveJdkInternals(f,multiReleaseJars,true,targetJreVersion);
			}else {
				dependentModules=resolveJdkInternals(f,normalJars,false,targetJreVersion);
			}
			for(String module:dependentModules) {
				modules.add(module);
			}
		}
		List<String> modules2=resolveJdkInternals(mainJar,normalJars,false,targetJreVersion);
		for(String module:modules2) {
			modules.add(module);
		}
		modules.add("jdk.charsets");
		String modulesStr=StringUtils.join(modules, ",");
		List<String> cmds=Arrays.asList("jlink","--compress",compressLevel,"--output",outputJreDirectory.getAbsolutePath(),"--add-modules",modulesStr);
		log.info("custom java runtime image cmd:{}",StringUtils.join(cmds, " "));
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
}
