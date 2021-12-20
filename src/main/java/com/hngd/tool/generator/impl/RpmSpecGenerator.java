package com.hngd.tool.generator.impl;

import com.hngd.tool.config.ConfigItems;

import com.hngd.tool.generator.ScriptGenerator;
import com.hngd.tool.util.VersionUtils;

import java.io.File;

import java.util.Map;

public class RpmSpecGenerator extends ScriptGenerator{

    public static final String SPEC ="sample.spec";
    public static final String RPMBUILD_BUILD ="rpmbuild-build.sh";
    public static final String SVC_RPM ="/templates/rpm/svc-rpm.sh";
    public static final String SCRIPT_TEMPLATE_ROOT="/templates/rpm";

    public RpmSpecGenerator() {
        super(SCRIPT_TEMPLATE_ROOT);
    }

    public void generateSpecFile(File outputDir, Map<String, Object> context) {
        //fix version
        String originVersion=context.get(ConfigItems.INNER_PROJECT_VERSION).toString();
        String newVersion= VersionUtils.fixToRpmVersion(originVersion);
        context.put(ConfigItems.INNER_PROJECT_VERSION,newVersion);
        doGenerateScript(context,outputDir,SPEC,context.get(ConfigItems.KEY_SERVICE_NAME)+".spec");
        //restore version
        context.put(ConfigItems.INNER_PROJECT_VERSION,originVersion);
        doGenerateScript(context,outputDir,RPMBUILD_BUILD,RPMBUILD_BUILD);

        doCopyResource(SVC_RPM,outputDir,"svc-rpm.sh");
    }
}
