package com.hngd.tool.generator.impl;

import java.io.File;
import java.io.IOException;

import java.util.Map;

import org.beetl.core.exception.BeetlException;

import com.hngd.tool.generator.ScriptGenerator;

public class WindowsServiceScriptGenerator extends ScriptGenerator {

    public static final String INSTALL="install.bat";
    public static final String RUN="run.bat";
    public static final String START="start.bat";
    public static final String STOP="stop.bat";
    public static final String UNINSTALL="uninstall.bat";
    public static final String SCRIPT_TEMPLATE_ROOT="/templates/windows";

    public WindowsServiceScriptGenerator() {
        super(SCRIPT_TEMPLATE_ROOT);
    }
    
    @Override
    public void generateServiceScript(File outputDir, Map<String, Object> context) throws BeetlException, IOException {

        doGenerateScript(context,outputDir,INSTALL,INSTALL);
        doGenerateScript(context,outputDir,START,START);
        doGenerateScript(context,outputDir,STOP,STOP);
        doGenerateScript(context,outputDir,UNINSTALL,UNINSTALL);
        //copy prunsrv.exe
        doCopyResource("/tools/prunsrv.exe",outputDir,"prunsrv.exe");

    }

    @Override
    public void generateConsoleScript(File outputDir, Map<String, Object> context) {
        doGenerateConsoleScript(context,outputDir,RUN,RUN,"run.%s.bat");
    }
}
