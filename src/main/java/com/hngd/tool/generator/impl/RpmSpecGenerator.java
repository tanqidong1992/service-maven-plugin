package com.hngd.tool.generator.impl;

import com.hngd.tool.config.ConfigItems;
import com.hngd.tool.exception.ScriptGenerationException;
import org.beetl.core.Configuration;
import org.beetl.core.GroupTemplate;
import org.beetl.core.Template;
import org.beetl.core.exception.BeetlException;
import org.beetl.core.resource.ClasspathResourceLoader;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;

import java.util.Map;

public class RpmSpecGenerator{

    public static final String SPEC ="sample.spec";
    public static final String SCRIPT_TEMPLATE_ROOT="/templates/rpm";
    GroupTemplate groupTemplate;
    public RpmSpecGenerator() {
        ClasspathResourceLoader resourceLoader = new ClasspathResourceLoader(SCRIPT_TEMPLATE_ROOT,"utf-8");
        Configuration cfg=null;
        try {
            cfg = Configuration.defaultConfiguration();
        } catch (IOException e) {
            throw new ScriptGenerationException("模板引擎初始化失败!",e);
        }
        groupTemplate = new GroupTemplate(resourceLoader, cfg);
    }

    public void generateSpecFile(File outputDir, Map<String, Object> context) {
        Template run=groupTemplate.getTemplate(SPEC);
        run.binding(context);
        File specFile=new File(outputDir,context.get(ConfigItems.KEY_SERVICE_NAME)+".spec");
        if(specFile.exists()){
            specFile.delete();
        }
        String script=run.render();
        try {
            Files.write(specFile.toPath(), script.getBytes(), StandardOpenOption.CREATE);
        } catch (BeetlException | IOException e) {
            throw new ScriptGenerationException("文件"+specFile.getAbsolutePath()+"写入操作错误!",e);
        }
    }
}
