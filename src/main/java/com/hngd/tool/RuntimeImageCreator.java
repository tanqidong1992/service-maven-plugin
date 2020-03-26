package com.hngd.tool;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeoutException;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.maven.plugin.MojoExecutionException;
import org.codehaus.plexus.util.FileUtils;
import org.zeroturnaround.exec.InvalidExitValueException;
import org.zeroturnaround.exec.ProcessExecutor;
import org.zeroturnaround.exec.ProcessResult;

import lombok.extern.slf4j.Slf4j;
@Slf4j
public class RuntimeImageCreator {

	public static List<String> resolveJdkInternals(File mainJar,List<File> libsJars,boolean isMultiReleaseJar,String jreVersion) throws InvalidExitValueException, IOException, InterruptedException, TimeoutException{
		List<String> jarFilePaths=libsJars.stream()
		    .map(f->f.getAbsolutePath())
		    .collect(Collectors.toList());
		String classpath=StringUtils.join(jarFilePaths, ";");
		String output=null;
		if(isMultiReleaseJar) {
			 output = new ProcessExecutor()
					 .command("jdeps","--print-module-deps","-q","--multi-release",jreVersion,"-cp",classpath,libsJars.get(0).getAbsolutePath())
	                .readOutput(true)
	                .redirectErrorAsDebug(log)
	                .execute()
	                .outputString("gbk");
		}else {
			output = new ProcessExecutor()
					.command("jdeps","--print-module-deps","-q","-cp",classpath,mainJar.getAbsolutePath())
	                .readOutput(true)
	                .redirectErrorAsDebug(log)
	                .execute()
	                .outputString("gbk");
		}
		return resolveModules(output);
		
	}
	//"--multi-release","11",
	public static List<String> resolveJdkInternals(File jarFile,String jreVersion) throws InvalidExitValueException, IOException, InterruptedException, TimeoutException{
		String output=null;
		if(isMultiReleaseJar(jarFile)) {
			ProcessResult pr= new ProcessExecutor().command("jdeps","--print-module-deps","--multi-release",jreVersion,jarFile.getAbsolutePath())
	                .readOutput(true)
	                .execute();
			 if(pr.getExitValue()!=0) {
				 return Collections.emptyList();
			 }
			output=pr.outputString("gbk");
		}else {
			ProcessResult pr = new ProcessExecutor().command("jdeps","--print-module-deps",jarFile.getAbsolutePath())
	                .readOutput(true)
	                .execute();
			if(pr.getExitValue()!=0) {
				 return Collections.emptyList();
			 }
			output=pr.outputString("gbk");
		}
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
		try {
			JarFile jarFile=new JarFile(jar);
			Manifest manifest=jarFile.getManifest();
			if(manifest==null) {
				return false;
			}
			Attributes a=manifest.getMainAttributes();
			Object value=null;
			for(Object key:a.keySet()) {
				if(key instanceof Attributes.Name) {
					if(((Attributes.Name)key).toString().equals("Multi-Release")) {
						value=a.get(key);
						break;
					}
				}
			}
			return "true".equals(value);
		} catch (IOException e) {
			log.error("",e);
		}
		return false;
	}
	

	public static void build(File mainJar, File dependentLibDirectory, File outputJreDirectory,String targetJreVersion) throws InvalidExitValueException, IOException, InterruptedException, TimeoutException {
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
			List<String> modules1=resolveJdkInternals(f, targetJreVersion);
			if(modules1.size()>0) {
				
			}else if(multiReleaseJars.contains(f)) {
				modules1=resolveJdkInternals(f,multiReleaseJars,true,targetJreVersion);
			}else {
				modules1=resolveJdkInternals(f,normalJars,false,targetJreVersion);
			}
			for(String module:modules1) {
				modules.add(module);
			}
		}
		List<String> modules2=resolveJdkInternals(mainJar,normalJars,false,targetJreVersion);
		for(String module:modules2) {
			modules.add(module);
		}
		modules.add("jdk.charsets");
		String modulesStr=StringUtils.join(modules, ",");
		System.out.println(modulesStr);
		ProcessResult result = new ProcessExecutor().command("jlink","--output",outputJreDirectory.getAbsolutePath(),"--add-modules",modulesStr)
                .readOutput(true)
                .execute();
		int exitValue=result.getExitValue();
		if(exitValue!=0) {
			String s=result.outputString("gbk");
			throw new RuntimeException(s, null);
		}
	}
}
