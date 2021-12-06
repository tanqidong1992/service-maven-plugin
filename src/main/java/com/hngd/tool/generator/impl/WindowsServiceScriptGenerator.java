package com.hngd.tool.generator.impl;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.beetl.core.Configuration;
import org.beetl.core.GroupTemplate;
import org.beetl.core.Template;
import org.beetl.core.exception.BeetlException;
import org.beetl.core.resource.ClasspathResourceLoader;

import com.hngd.tool.ScriptGeneratorContext;
import com.hngd.tool.config.ConfigItems;
import com.hngd.tool.exception.ScriptGenerationException;
import com.hngd.tool.generator.ScriptGenerator;

public class WindowsServiceScriptGenerator implements ScriptGenerator {

    public static final String INSTALL="install.bat";
    public static final String RUN="run.bat";
    public static final String START="start.bat";
    public static final String STOP="stop.bat";
    public static final String UNINSTALL="uninstall.bat";
    public static final String SCRIPT_TEMPLATE_ROOT="/templates/windows";
    
    GroupTemplate groupTemplate;
    public WindowsServiceScriptGenerator() {
        ClasspathResourceLoader resourceLoader = new ClasspathResourceLoader(SCRIPT_TEMPLATE_ROOT,"utf-8");
        Configuration cfg=null;
        try {
            cfg = Configuration.defaultConfiguration();
        } catch (IOException e) {
            throw new ScriptGenerationException("模板引擎初始化失败!",e);
        }
        groupTemplate = new GroupTemplate(resourceLoader, cfg);
    }
    
    @Override
    public void generateServiceScript(File outputDir, Map<String, Object> context) throws BeetlException, IOException {
        Template install = groupTemplate.getTemplate(INSTALL);
        install.binding(context);
        Files.write(new File(outputDir,INSTALL).toPath(), install.render().getBytes(), StandardOpenOption.CREATE);
         
        Template start=groupTemplate.getTemplate(START);
        start.binding(context);
        Files.write(new File(outputDir,START).toPath(), start.render().getBytes(), StandardOpenOption.CREATE);
        
        Template stop=groupTemplate.getTemplate(STOP);
        stop.binding(context);
        Files.write(new File(outputDir,STOP).toPath(), stop.render().getBytes(), StandardOpenOption.CREATE);
        
        Template uninstall=groupTemplate.getTemplate(UNINSTALL);
        uninstall.binding(context);
        Files.write(new File(outputDir,UNINSTALL).toPath(), uninstall.render().getBytes(), StandardOpenOption.CREATE);
        
        //copy prunsrv.exe
        File file=new File(outputDir,"prunsrv.exe");
        if(file.exists()) {
            file.delete();
        }
        try(InputStream in=ScriptGeneratorContext.class.getResourceAsStream("/tools/prunsrv.exe")){
            Files.copy(in, file.toPath(), StandardCopyOption.REPLACE_EXISTING);
        }catch(IOException e) {
            throw e;
        }

    }

    @Override
    public void generateConsoleScript(File outputDir, Map<String, Object> context) {
        Template run=groupTemplate.getTemplate(RUN);
        run.binding(context);
        File runBatFile=new File(outputDir,RUN);
        String script=run.render();
        try {
            Files.write(runBatFile.toPath(), script.getBytes(), StandardOpenOption.CREATE);
        } catch (BeetlException | IOException e) {
            throw new ScriptGenerationException("文件"+runBatFile.getAbsolutePath()+"写入操作错误!",e);
        }
        String s=(String) context.get(ConfigItems.KEY_ADDITIONAL_MAIN_CLASS);
        if(s==null || s.length()==0) {
            return ;
        }
        List<String> additionalMainClassNames=new LinkedList<>();
        if(s.contains(",")) {
            String[] mainClasses=s.split(",");
            for(String mainClass:mainClasses) {
                additionalMainClassNames.add(mainClass);
            }
        }else {
            additionalMainClassNames.add(s);
        }
        String mainClassName=(String) context.get(ConfigItems.KEY_MAIN_CLASS);
        for(String additionalMainClassName:additionalMainClassNames) {
            String newScript=script.replace(mainClassName, additionalMainClassName);
            File additionalRunBatFile=new File(outputDir,"run."+additionalMainClassName+".bat");
            try {
                Files.write(additionalRunBatFile.toPath(), newScript.getBytes(), StandardOpenOption.CREATE);
            } catch (BeetlException | IOException e) {
                throw new ScriptGenerationException("文件"+additionalRunBatFile.getAbsolutePath()+"写入操作错误!",e);
            }
        }
    }
}
