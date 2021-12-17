package com.hngd.tool.generator.impl;

import java.io.File;
import java.io.IOException;

import java.util.List;
import java.util.Map;

import com.hngd.tool.util.JreUtils;
import com.hngd.tool.util.ScriptCompiler;
import com.hngd.tool.util.ScriptUtils;
import org.beetl.core.exception.BeetlException;

import com.hngd.tool.generator.ScriptGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SystemdScriptGenerator extends ScriptGenerator {

    private static final Logger logger= LoggerFactory.getLogger(SystemdScriptGenerator.class);
    public static final String ENV="env.sh";
    public static final String RUN="run-foreground.sh";
    public static final String SCRIPT_TEMPLATE_ROOT="/templates/systemd";

    public SystemdScriptGenerator() {
        super(SCRIPT_TEMPLATE_ROOT);
    }

    @Override
    public void generateServiceScript(File outputDir, Map<String, Object> context)  throws BeetlException, IOException{
        doGenerateScript(context,outputDir,ENV,ENV);
        //copy svc.sh
        File svc=doCopyResource("/templates/systemd/svc.sh",outputDir,"svc.sh");
        if(JreUtils.isLinux()) {
            ScriptCompiler.getInstance().compile(svc, svc);
        }
        ScriptUtils.addExecutePermission(svc);
        //copy service unit template
        doCopyResource("/templates/systemd/sample.service",outputDir,"sample.service");
    }

    @Override
    public void generateConsoleScript(File outputDir, Map<String, Object> context) {
        List<File> files= doGenerateConsoleScript(context,outputDir,RUN,RUN,"run-foreground.%s.sh");
        for (File file:files){
            try {
                ScriptUtils.addExecutePermission(file);
            } catch (IOException e) {
                logger.error("",e);
            }
        }
    }

}
