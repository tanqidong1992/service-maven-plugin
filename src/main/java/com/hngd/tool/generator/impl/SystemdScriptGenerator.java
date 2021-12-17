package com.hngd.tool.generator.impl;

import java.io.File;
import java.io.IOException;

import java.util.Map;

import org.beetl.core.exception.BeetlException;

import com.hngd.tool.generator.ScriptGenerator;

public class SystemdScriptGenerator extends ScriptGenerator {

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
        doCopyResource("/templates/systemd/svc.sh",outputDir,"svc.sh");
        //copy service unit template
        doCopyResource("/templates/systemd/sample.service",outputDir,"sample.service");
    }

    @Override
    public void generateConsoleScript(File outputDir, Map<String, Object> context) {
        doGenerateConsoleScript(context,outputDir,RUN,RUN,"run-foreground.%s.sh");
    }

}
