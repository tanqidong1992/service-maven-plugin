package com.hngd.tool.generator;

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

public class SystemdScriptGenerator implements ScriptGenerator {

	public static final String ENV="env.sh";
	public static final String RUN="run-foreground.sh";
	public static final String SCRIPT_TEMPLATE_ROOT="/templates/systemd";
	GroupTemplate groupTemplate;
	public SystemdScriptGenerator() {
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
	public void generateDaemonScript(File workdir, Map<String, Object> context)  throws BeetlException, IOException{
		Template env = groupTemplate.getTemplate(ENV);
    	env.binding(context);
		Files.write(new File(workdir,ENV).toPath(), env.render().getBytes(), StandardOpenOption.CREATE);
		//copy svc.sh
        File svcFile=new File(workdir,"svc.sh");
        if(svcFile.exists()) {
        	svcFile.delete();
        }
        try(InputStream in=ScriptGeneratorContext.class.getResourceAsStream("/templates/systemd/svc.sh")){
        	Files.copy(in, svcFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        }catch(IOException e) {
        	throw e;
		}
		//copy service unit template
		File serviceUnitTemplateFile=new File(workdir,"sample.service");
        if(serviceUnitTemplateFile.exists()) {
        	serviceUnitTemplateFile.delete();
        }
        try(InputStream in=ScriptGeneratorContext.class.getResourceAsStream("/templates/systemd/sample.service")){
        	Files.copy(in, serviceUnitTemplateFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        }catch(IOException e) {
        	throw e;
		}

	}

	@Override
	public void generateConsoleScript(File workdir, Map<String, Object> context) {
		Template run=groupTemplate.getTemplate(RUN);
    	run.binding(context);
    	File runBatFile=new File(workdir,RUN);
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
    		File runBatFile1=new File(workdir,"run-foreground."+additionalMainClassName+".sh");
        	try {
    			Files.write(runBatFile1.toPath(), newScript.getBytes(), StandardOpenOption.CREATE);
    		} catch (BeetlException | IOException e) {
    			throw new ScriptGenerationException("文件"+runBatFile1.getAbsolutePath()+"写入操作错误!",e);
    		}
    	}

	}

}
