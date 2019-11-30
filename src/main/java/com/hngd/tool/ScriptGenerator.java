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
import java.util.Optional;
import java.util.Properties;

import org.apache.maven.project.MavenProject;
import org.beetl.core.Configuration;
import org.beetl.core.GroupTemplate;
import org.beetl.core.Template;
import org.beetl.core.exception.BeetlException;
import org.beetl.core.resource.ClasspathResourceLoader;
import org.codehaus.plexus.util.StringUtils;

import com.hngd.tool.config.ConfigItems;
import com.hngd.tool.exception.ScriptGenerationException;
import com.hngd.tool.utils.ClassWeight;
import com.hngd.tool.utils.MainClassDetector;

public class ScriptGenerator {

	public static final String INSTALL="install.bat";
	public static final String RUN="run.bat";
	public static final String START="start.bat";
	public static final String STOP="stop.bat";
	public static final String UNINSTALL="uninstall.bat";

	public static final String KEY_CLASS_PATH="classPath";
	
	public static final String SCRIPT_TEMPLATE_ROOT="/scripts";
	
    public static void generateScripts(MavenProject mavenProject, File configFile,File workdir,File dependenciesDirectory,File jarFile) throws ScriptGenerationException{
    	 
    	String configPath=configFile!=null?configFile.getAbsolutePath():null;
		Properties properties=new Properties();
		if(StringUtils.isNotEmpty(configPath)) {
			properties=loadConfig(configPath);
		}
    	ClasspathResourceLoader resourceLoader = new ClasspathResourceLoader(SCRIPT_TEMPLATE_ROOT,"utf-8");
    	Configuration cfg=null;
		try {
			cfg = Configuration.defaultConfiguration();
		} catch (IOException e) {
			throw new ScriptGenerationException("模板引擎初始化失败!",e);
		}
    	GroupTemplate groupTemplate = new GroupTemplate(resourceLoader, cfg);
    	
    	Map<String,Object> mavenContext=initializeMavenContext(mavenProject,jarFile.getAbsolutePath());
    	fixAbsentProperties(properties,mavenContext);
    	Map<String,Object> context=initializeContext(properties,dependenciesDirectory,jarFile);
    	if("true".equals(context.get(ConfigItems.KEY_SUPPORT_SERVICE))) {
    		try {
				generateServiceScript(workdir,groupTemplate,context);
			} catch (BeetlException | IOException e) {
				throw new ScriptGenerationException("生成服务脚本错误!",e);
			}
    	}
    	 
        Template run=groupTemplate.getTemplate(RUN);
    	run.binding(context);
    	File runBatFile=new File(workdir,RUN);
    	try {
			Files.write(runBatFile.toPath(), run.render().getBytes(), StandardOpenOption.CREATE);
		} catch (BeetlException | IOException e) {
			throw new ScriptGenerationException("文件"+runBatFile.getAbsolutePath()+"写入操作错误!",e);
		}
    }

	private static void fixAbsentProperties(Properties properties, Map<String, Object> mavenContext) {
		
		if(!properties.containsKey(ConfigItems.KEY_MAIN_CLASS)) {
			
			if(!mavenContext.containsKey(ConfigItems.INNER_PROJECT_MAIN_CLASS)) {
				throw new ScriptGenerationException("没有找到合适的启动类", null);
			}
			Object mainClass=mavenContext.get(ConfigItems.INNER_PROJECT_MAIN_CLASS);
			properties.put(ConfigItems.KEY_MAIN_CLASS, mainClass);
		}
		
		if(!properties.containsKey(ConfigItems.KEY_SERVICE_NAME)) {
			Object serviceName=mavenContext.get(ConfigItems.INNER_PROJECT_NAME);
			properties.put(ConfigItems.KEY_SERVICE_NAME, serviceName);
		}
		
		if(!properties.containsKey(ConfigItems.KEY_SERVICE_DESCRIPTION)) {
			if(mavenContext.containsKey(ConfigItems.INNER_PROJECT_DESCRIPTION)){
				Object serviceDescription=mavenContext.get(ConfigItems.INNER_PROJECT_DESCRIPTION);
				properties.put(ConfigItems.KEY_SERVICE_DESCRIPTION, serviceDescription);
			}
		}
		
		if(!properties.containsKey(ConfigItems.KEY_SUPPORT_SERVICE)) {
			if(mavenContext.containsKey(ConfigItems.INNER_PROJECT_MAIN_CLASS_SUPPORT_SERVICE)){
				Object mainClassSupportService=mavenContext.get(ConfigItems.INNER_PROJECT_MAIN_CLASS_SUPPORT_SERVICE);
				properties.put(ConfigItems.KEY_SUPPORT_SERVICE, mainClassSupportService);
			}
		}
		 
	}

	private static Map<String, Object> initializeMavenContext(MavenProject mavenProject,String mainJarFilePath) {
		Map<String,Object> context=new HashMap<>();
		String name=mavenProject.getName();
		String artifactId=mavenProject.getArtifactId();
		String description=mavenProject.getDescription();
		Optional<ClassWeight> optionalMainClass=Optional.empty();
		try {
			optionalMainClass = MainClassDetector.findTheMostAppropriateMainClass(mainJarFilePath);
		} catch (IOException e) {
			e.printStackTrace();
		}
		if(optionalMainClass.isPresent()) {
			context.put(ConfigItems.INNER_PROJECT_MAIN_CLASS, optionalMainClass.get().name);
			if(optionalMainClass.get().weight>=3) {
				context.put(ConfigItems.INNER_PROJECT_MAIN_CLASS_SUPPORT_SERVICE, "true");
			}
		}
		if(StringUtils.isNotBlank(description)) {
			context.put(ConfigItems.INNER_PROJECT_DESCRIPTION, description);
		}
		if(StringUtils.isNotBlank(name)) {
			context.put(ConfigItems.INNER_PROJECT_NAME, name);
		}else {
			context.put(ConfigItems.INNER_PROJECT_NAME, artifactId);
		}
		return context;
	}

	private static Map<String, Object> initializeContext(Properties properties, File dependenciesDirectory,File jarFile) {
		Map<String,Object> context=new HashMap<>();
    	String classpath=processClassPath(dependenciesDirectory,jarFile);
    	context.put(KEY_CLASS_PATH, classpath);
    	ConfigItems.getAllConfigItems().stream()
    	    .map(item->item.loadValue(properties))
    	    .filter(value->value.getValue()!=null)
    	    .forEach(value->{
    	    	context.put(value.getName(),value.getValue());
    	    });
		return context;
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
        	throw e;
        }
		
	}

	private static Properties loadConfig(String configPath) {
		Properties properties=new Properties();
		try(InputStream in=new FileInputStream(configPath);
			Reader reader=new InputStreamReader(in, "utf-8");){
			properties.load(reader);
		} catch (IOException e) {
			if(e instanceof FileNotFoundException){
				throw new RuntimeException("配置文件"+configPath+"不存在",e);
			}else {
				throw new RuntimeException("配置文件"+configPath+"读取失败",e);
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
