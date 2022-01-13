package com.hngd.tool.generator;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.*;
import java.util.stream.Collectors;

import com.hngd.tool.generator.impl.RpmSpecGenerator;
import org.apache.maven.model.License;
import org.apache.maven.project.MavenProject;
import org.beetl.core.exception.BeetlException;
import org.codehaus.plexus.util.StringUtils;

import com.hngd.tool.config.ConfigItems;
import com.hngd.tool.config.NameValuePair;
import com.hngd.tool.constant.Constants;
import com.hngd.tool.constant.ServiceTypes;
import com.hngd.tool.exception.ScriptGenerationException;

import com.hngd.tool.generator.impl.WindowsServiceScriptGenerator;
import com.hngd.tool.generator.impl.SystemdScriptGenerator;
import com.hngd.tool.util.ClassWeight;
import com.hngd.tool.util.MainClassDetector;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ScriptGeneratorContext {

    public static void generateScripts(MavenProject mavenProject, 
            File configFile,
            File outputDir,
            File dependenciesDirectory,
            File jarFile,
            String serviceType,
            Boolean outputRpmSpec,
            Map<String,String> mavenParameterContext) throws ScriptGenerationException{
         
        String configPath=configFile!=null?configFile.getAbsolutePath():null;
        Properties properties=new Properties();
        if(StringUtils.isNotEmpty(configPath)) {
            properties=loadConfig(configPath);
        }
        ScriptGenerator scriptGenerator;
        if(ServiceTypes.WINDOWS.equals(serviceType)) {
            scriptGenerator=new WindowsServiceScriptGenerator();
        }else {
            scriptGenerator=new SystemdScriptGenerator();
        }
        Map<String,Object> mavenContext=initializeMavenContext(mavenProject,jarFile.getAbsolutePath(),serviceType);
        fixAbsentProperties(properties,mavenContext);
        Map<String,Object> context=initializeConfigContext(properties,dependenciesDirectory,jarFile,serviceType);
        context.putAll(mavenContext);
        context.putAll(mavenParameterContext);
        if(Boolean.TRUE.toString().equals(context.get(ConfigItems.KEY_SUPPORT_SERVICE))) {
            try {
                scriptGenerator.generateServiceScript(outputDir, context);
            } catch (BeetlException | IOException e) {
                throw new ScriptGenerationException("", e);
            }
        }
        scriptGenerator.generateConsoleScript(outputDir, context);
        if(outputRpmSpec && ServiceTypes.SYSTEMD.equals(serviceType)){
            new RpmSpecGenerator().generateSpecFile(outputDir.getParentFile(),context);
        }
    }

    private static void fixAbsentProperties(Properties properties, Map<String, Object> mavenContext) {
        
        if(!properties.containsKey(ConfigItems.KEY_MAIN_CLASS)) {
            
            if(!mavenContext.containsKey(ConfigItems.INNER_PROJECT_MAIN_CLASS)) {
                throw new ScriptGenerationException("No suitable main class was found!", null);
            }
            Object mainClass=mavenContext.get(ConfigItems.INNER_PROJECT_MAIN_CLASS);
            properties.put(ConfigItems.KEY_MAIN_CLASS, mainClass);
        }
        
        if(!properties.containsKey(ConfigItems.KEY_SERVICE_NAME)) {
            Object serviceName=mavenContext.get(ConfigItems.INNER_PROJECT_NAME);
            properties.put(ConfigItems.KEY_SERVICE_NAME, serviceName);
        }
        
        if(!properties.containsKey(ConfigItems.KEY_SERVICE_DESCRIPTION) 
                && mavenContext.containsKey(ConfigItems.INNER_PROJECT_DESCRIPTION)) {
            
            Object serviceDescription=mavenContext.get(ConfigItems.INNER_PROJECT_DESCRIPTION);
            properties.put(ConfigItems.KEY_SERVICE_DESCRIPTION, serviceDescription);
             
        }
        
        if(!properties.containsKey(ConfigItems.KEY_SUPPORT_SERVICE) 
            && mavenContext.containsKey(ConfigItems.INNER_PROJECT_MAIN_CLASS_SUPPORT_SERVICE)){
            
            Object mainClassSupportService=mavenContext.get(ConfigItems.INNER_PROJECT_MAIN_CLASS_SUPPORT_SERVICE);
            properties.put(ConfigItems.KEY_SUPPORT_SERVICE, mainClassSupportService);
        }

    }

    private static Map<String, Object> initializeMavenContext(MavenProject mavenProject,
                                                              String mainJarFilePath,
                                                              String serviceType) {
        Map<String,Object> context=new HashMap<>();
        String name=mavenProject.getName();
        String artifactId=mavenProject.getArtifactId();
        String description=mavenProject.getDescription();
        Optional<ClassWeight> optionalMainClass=Optional.empty();
        try {
            optionalMainClass = MainClassDetector.findTheMostAppropriateMainClass(mainJarFilePath);
        } catch (IOException e) {
            log.error("",e);
        }
        if(optionalMainClass.isPresent()) {
            ClassWeight mainClass=optionalMainClass.get();
            context.put(ConfigItems.INNER_PROJECT_MAIN_CLASS, mainClass.name);
            //main onStart onStop
            if(mainClass.weight>=3 || ServiceTypes.SYSTEMD.equals(serviceType)) {
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

        List<License> licenses=mavenProject.getLicenses();
        if(licenses!=null && licenses.size()>0){
            String licenseStr=licenses.stream()
                    .map(License::getName)
                    .collect(Collectors.joining(","));
            context.put(ConfigItems.INNER_PROJECT_LICENSE,licenseStr);
        }
        String version=mavenProject.getVersion();
        if(StringUtils.isNotBlank(version)){
            context.put(ConfigItems.INNER_PROJECT_VERSION,version);
        }
        String url= mavenProject.getUrl();
        if(StringUtils.isNotBlank(url)){
            context.put(ConfigItems.INNER_PROJECT_URL,url);
        }
        return context;
    }

    private static Map<String, Object> initializeConfigContext(Properties properties, File dependenciesDirectory,File jarFile,String serviceType) {
        boolean isUnixStyle=ServiceTypes.SYSTEMD.equals(serviceType);
        Map<String,Object> context=new HashMap<>();
        String classpath=processClassPath(dependenciesDirectory,jarFile,isUnixStyle);
        context.put(Constants.KEY_CLASS_PATH, classpath);
        ConfigItems.getAllConfigItems().stream()
            .map(item->item.loadValue(properties))
            .filter(NameValuePair::isValuePresent)
            .forEach(nameValuePair->context.put(nameValuePair.getName(),nameValuePair.getValue()));
        return context;
    }
  
    private static Properties loadConfig(String configPath) {
        Properties properties=new Properties();
        try(InputStream in=new FileInputStream(configPath);
            Reader reader=new InputStreamReader(in, Constants.DEFAULT_CHARSET_NAME);){
            properties.load(reader);
        } catch (IOException e) {
            if(e instanceof FileNotFoundException){
                throw new RuntimeException("The configuration file ["+configPath+"] dose not exist!",e);
            }else {
                throw new RuntimeException("Failed to read the configuration file: "+configPath,e);
            }
        } 
        return properties;
    }

    private static String processClassPath(File dependenciesDirectory,File mainJarFile,boolean isUnixStyle) {
        String delimiter=isUnixStyle?":":";";
        String fileSeparator=isUnixStyle?"/":"\\";
        String prefix=isUnixStyle?"${APP_BASE_DIR}":"%APP_BASE_DIR%";
        File files[] = dependenciesDirectory.listFiles();
        StringBuilder sb=new StringBuilder();
        if(files!=null){
            /**
            for (File file:files){
                String relativeFilePath = prefix+fileSeparator+file.getParentFile().getName()+fileSeparator+file.getName();
                sb.append(relativeFilePath + delimiter);
            }
            */
            sb.append(prefix+fileSeparator+dependenciesDirectory.getName()+fileSeparator).append("*").append(delimiter);
        }
        sb.append(prefix+fileSeparator+mainJarFile.getName());
        return sb.toString();
    }
}
