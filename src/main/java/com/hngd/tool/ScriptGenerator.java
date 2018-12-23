package com.hngd.tool;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.beetl.core.Configuration;
import org.beetl.core.GroupTemplate;
import org.beetl.core.Template;
import org.beetl.core.exception.BeetlException;
import org.beetl.core.resource.ClasspathResourceLoader;
import org.beetl.core.resource.FileResourceLoader;

public class ScriptGenerator {

	public static final String INSTALL="install.bat";
	public static final String RUN="run.bat";
	public static final String START="start.bat";
	public static final String STOP="stop.bat";
	public static final String UNINSTALL="uninstall.bat";
	public static final String KEY_SUPPORT_SERVICE="supportService";
	
	public static final String KEY_JAVA_RUN_OPTIONS="javaRunOptions";
	public static final String KEY_JVM_MS="jvmMs";
	public static final String KEY_JVM_MX="jvmMx";
	public static final String DEFAULT_JVM_MS="256m";
	public static final String DEFAULT_JVM_MX="512m";
	public static final String KEY_SERVICE_DESCRIPTION="serviceDescription";
	public static final String KEY_SERVICE_DISPLAY_NAME="serviceDisplayName";
	public static final String KEY_SERVICE_NAME="serviceName";
	private static final String SCRIPT_TEMPLATE_ROOT="/scripts";
	public static final String KEY_CLASS_PATH="classPath";
	private static final String DEFAULT_JAVA_RUN_OPTIONS = "";
    public static void generateScripts(File configFile,File workdir,File dependenciesDirectory,File jarFile) throws IOException
    {
    	 
    	String configPath=configFile.getAbsolutePath();
		Properties properties=loadConfig(configPath);
    	ClasspathResourceLoader resourceLoader = new ClasspathResourceLoader(SCRIPT_TEMPLATE_ROOT,"utf-8");
    	Configuration cfg = Configuration.defaultConfiguration();
    	GroupTemplate groupTemplate = new GroupTemplate(resourceLoader, cfg);
    	Map<String,Object> context=new HashMap<>();
    	context.put(KEY_JAVA_RUN_OPTIONS, DEFAULT_JAVA_RUN_OPTIONS);
    	context.put(KEY_JVM_MS, DEFAULT_JVM_MS);
    	context.put(KEY_JVM_MX, DEFAULT_JVM_MX);
    	String classpath=processClassPath(dependenciesDirectory,jarFile);
    	context.put(KEY_CLASS_PATH, classpath);
    	properties.forEach((k,v)->context.put((String) k, v));
    	String serviceName=properties.getProperty(KEY_SERVICE_NAME);
    	if(properties.getProperty(KEY_SERVICE_DISPLAY_NAME)==null){
    		properties.setProperty(KEY_SERVICE_DISPLAY_NAME, serviceName);
    	}
    	if(properties.getProperty(KEY_SERVICE_DESCRIPTION)==null){
    		properties.setProperty(KEY_SERVICE_DESCRIPTION, serviceName);
    	}
    	String supportService=properties.getProperty(KEY_SUPPORT_SERVICE);
    	if("true".equals(supportService)) {
    		generateServiceScript(workdir,groupTemplate,context);
    	}
    	 
        Template run=groupTemplate.getTemplate(RUN);
    	run.binding(context);
    	Files.write(new File(workdir,RUN).toPath(), run.render().getBytes(), StandardOpenOption.CREATE);
    }

	private static void generateServiceScript(File workdir,GroupTemplate groupTemplate, Map<String, Object> context) throws BeetlException, IOException {
		Template install = groupTemplate.getTemplate(INSTALL);
    	install.binding(context);
    	Files.write(new File(workdir,INSTALL).toPath(), install.render().getBytes(), StandardOpenOption.CREATE);
    	 
    	Template start=groupTemplate.getTemplate(START);
    	start.binding(context);
        Files.write(new File(workdir,START).toPath(), start.render().getBytes(), StandardOpenOption.CREATE);
        
        Template stop=groupTemplate.getTemplate(STOP);
        stop.binding(context);
        Files.write(new File(workdir,STOP).toPath(), stop.render().getBytes(), StandardOpenOption.CREATE);
        
        Template uninstall=groupTemplate.getTemplate(UNINSTALL);
        uninstall.binding(context);
        Files.write(new File(workdir,UNINSTALL).toPath(), uninstall.render().getBytes(), StandardOpenOption.CREATE);
	    
        //copy prunsrv.exe
        File file=new File(workdir,"prunsrv.exe");
        if(file.exists()) {
        	file.delete();
        }
        try(InputStream in=ScriptGenerator.class.getResourceAsStream("/tools/prunsrv.exe")){
        	Files.copy(in, file.toPath(), StandardCopyOption.REPLACE_EXISTING);
        }catch(IOException e) {
        	e.printStackTrace();
        }
		
	}

	private static Properties loadConfig(String configPath) {
		Properties properties=new Properties();
		try(InputStream in=new FileInputStream(configPath);
			Reader reader=new InputStreamReader(in, "utf-8");){
			properties.load(reader);
		} catch (IOException e) {
			if(e instanceof FileNotFoundException){
				throw new RuntimeException("配置文件不存在",e);
			}else {
				throw new RuntimeException("配置文件读取失败",e);
			}
		} 
		return properties;
	}

	private static String processClassPath(File dependenciesDirectory,File jarFile) {
		File files[] = dependenciesDirectory.listFiles();
		StringBuilder sb=new StringBuilder();
		if(files!=null){
			for (File file:files){
	            String relativeFilePath = "."+File.separator+file.getParentFile().getName()+File.separator+file.getName();
	            sb.append(relativeFilePath + ";");
	        } 
		}
        sb.append(jarFile.getName());
		return sb.toString();
	}
}
