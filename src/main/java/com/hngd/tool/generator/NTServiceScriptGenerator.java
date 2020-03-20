package com.hngd.tool.generator;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.Map;

import org.beetl.core.Configuration;
import org.beetl.core.GroupTemplate;
import org.beetl.core.Template;
import org.beetl.core.exception.BeetlException;
import org.beetl.core.resource.ClasspathResourceLoader;

import com.hngd.tool.ScriptGeneratorContext;
import com.hngd.tool.exception.ScriptGenerationException;

public class NTServiceScriptGenerator implements ScriptGenerator {

	public static final String INSTALL="install.bat";
	public static final String RUN="run.bat";
	public static final String START="start.bat";
	public static final String STOP="stop.bat";
	public static final String UNINSTALL="uninstall.bat";
	public static final String SCRIPT_TEMPLATE_ROOT="/templates/ntservice";
	
	GroupTemplate groupTemplate;
	public NTServiceScriptGenerator() {
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
	public void generateDaemonScript(File workdir, Map<String, Object> context) throws BeetlException, IOException {
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
        try(InputStream in=ScriptGeneratorContext.class.getResourceAsStream("/tools/prunsrv.exe")){
        	Files.copy(in, file.toPath(), StandardCopyOption.REPLACE_EXISTING);
        }catch(IOException e) {
        	throw e;
        }

	}

	@Override
	public void generateConsoleScript(File workdir, Map<String, Object> context) {
		Template run=groupTemplate.getTemplate(RUN);
    	run.binding(context);
    	File runBatFile=new File(workdir,RUN);
    	try {
			Files.write(runBatFile.toPath(), run.render().getBytes(), StandardOpenOption.CREATE);
		} catch (BeetlException | IOException e) {
			throw new ScriptGenerationException("文件"+runBatFile.getAbsolutePath()+"写入操作错误!",e);
		}
	}
}
