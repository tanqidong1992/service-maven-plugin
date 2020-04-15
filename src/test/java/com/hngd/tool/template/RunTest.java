package com.hngd.tool.template;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.beetl.core.Configuration;
import org.beetl.core.GroupTemplate;
import org.beetl.core.Template;
import org.beetl.core.resource.ClasspathResourceLoader;

import com.hngd.tool.ScriptGeneratorContext;
import com.hngd.tool.constant.Constants;
import com.hngd.tool.exception.ScriptGenerationException;
import com.hngd.tool.generator.impl.NTServiceScriptGenerator;

public class RunTest {

	public static void main(String[] args) {
		
		ClasspathResourceLoader resourceLoader = new ClasspathResourceLoader(NTServiceScriptGenerator.SCRIPT_TEMPLATE_ROOT,"utf-8");
    	Configuration cfg=null;
		try {
			cfg = Configuration.defaultConfiguration();
		} catch (IOException e) {
			throw new ScriptGenerationException("模板引擎初始化失败!",e);
		}
		GroupTemplate groupTemplate = new GroupTemplate(resourceLoader, cfg);
    	Map<String,Object> context=new HashMap<>();
    	context.put(Constants.KEY_CLASS_PATH, "123");
    	context.put("mainClass", "com.tqd.A");
    	Template tpl=groupTemplate.getTemplate(NTServiceScriptGenerator.RUN);
    	tpl.binding(context);
    	String s=tpl.render();
    	System.out.println(s);

	}

}
