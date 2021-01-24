package com.hngd.tool.generator;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import org.beetl.core.exception.BeetlException;

public interface ScriptGenerator {
    /**
     * 生成服务操作脚本
     * @param outputDir 输出目录
     * @param context 模板变量
     * @throws BeetlException
     * @throws IOException
     */
    public void generateServiceScript(File outputDir, Map<String, Object> context) throws BeetlException, IOException;
    /**
     * 生成控制台启动脚本
     * @param outputDir 输出目录
     * @param context 模板变量
     */
    public void generateConsoleScript(File outputDir, Map<String, Object> context);
    
}
