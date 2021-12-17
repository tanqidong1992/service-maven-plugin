package com.hngd.tool.generator;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.hngd.tool.ScriptGeneratorContext;
import com.hngd.tool.config.ConfigItems;
import com.hngd.tool.constant.Constants;
import com.hngd.tool.exception.ScriptGenerationException;
import org.beetl.core.Configuration;
import org.beetl.core.GroupTemplate;
import org.beetl.core.Template;
import org.beetl.core.exception.BeetlException;
import org.beetl.core.resource.ClasspathResourceLoader;

public abstract class ScriptGenerator {

    protected GroupTemplate groupTemplate;

    public ScriptGenerator(String scriptTemplateRoot) {
        ClasspathResourceLoader resourceLoader = new ClasspathResourceLoader(scriptTemplateRoot, Constants.DEFAULT_CHARSET_NAME);
        groupTemplate = new GroupTemplate(resourceLoader, loadDefaultConfiguration());
    }

    protected String doGenerateScript(Map<String,Object> context,File outputDir,String templateName,String targetFileName){
        Template template = groupTemplate.getTemplate(templateName);
        template.binding(context);
        File target=new File(outputDir,targetFileName);
        String script=template.render();
        try {
            Files.write(target.toPath(), script.getBytes(), StandardOpenOption.CREATE);
        } catch (BeetlException | IOException e) {
            throw new ScriptGenerationException("File Write error:"+target.getAbsolutePath(),e);
        }
        return script;
    }

    protected void doGenerateConsoleScript(Map<String,Object> context,File outputDir,String templateName,String targetFileName,
                                           String additionalFileNamePattern){
        Template run=groupTemplate.getTemplate(templateName);
        File runBatFile=new File(outputDir,targetFileName);
        String script=ScriptGenerator.renderAndWrite(context,run,runBatFile);
        List<String> additionalMainClassNames=ScriptGenerator.parseAdditionalMainClassNames(context);
        String mainClassName=(String) context.get(ConfigItems.KEY_MAIN_CLASS);
        for(String additionalMainClassName:additionalMainClassNames) {
            String newScript=script.replace(mainClassName, additionalMainClassName);
            String additionalFileName=String.format(additionalFileNamePattern,additionalMainClassName);
            File additionalRunBatFile=new File(outputDir,additionalFileName);
            try {
                Files.write(additionalRunBatFile.toPath(), newScript.getBytes(), StandardOpenOption.CREATE);
            } catch (BeetlException | IOException e) {
                throw new ScriptGenerationException("File write error:"+additionalRunBatFile.getAbsolutePath(),e);
            }
        }
    }

    protected void doCopyResource(String resourcePath,File outputDir,String targetFileName){
        File targetFile=new File(outputDir,targetFileName);
        if(targetFile.exists()) {
            targetFile.delete();
        }
        try(InputStream in= ScriptGeneratorContext.class.getResourceAsStream(resourcePath)){
            Files.copy(in, targetFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        }catch(IOException e) {
            throw new ScriptGenerationException("Failed to copy Resource ["+resourcePath+"] into ["+targetFile.getAbsolutePath()+"]",e);
        }
    }

    /**
     * 生成服务操作脚本
     * @param outputDir 输出目录
     * @param context 模板变量
     * @throws BeetlException
     * @throws IOException
     */
    public void generateServiceScript(File outputDir, Map<String, Object> context) throws BeetlException, IOException{};
    /**
     * 生成控制台启动脚本
     * @param outputDir 输出目录
     * @param context 模板变量
     */
    public void generateConsoleScript(File outputDir, Map<String, Object> context){};


    public static String renderAndWrite(Map<String, Object> context, Template template,File target){
        template.binding(context);
        String script=template.render();
        if(target.exists()){
            target.delete();
        }
        try {
            Files.write(target.toPath(), script.getBytes(Constants.DEFAULT_CHARSET_NAME), StandardOpenOption.CREATE);
        } catch (BeetlException | IOException e) {
            throw new ScriptGenerationException("File Write error:"+target.getAbsolutePath(),e);
        }
        return script;
    }

    public static Configuration loadDefaultConfiguration(){
        Configuration cfg=null;
        try {
            cfg = Configuration.defaultConfiguration();
        } catch (IOException e) {
            throw new ScriptGenerationException("Failed to initialize the template engine default configuration!",e);
        }
        return cfg;
    }

    public static List<String> parseAdditionalMainClassNames(Map<String,Object> context){
        String s=(String) context.get(ConfigItems.KEY_ADDITIONAL_MAIN_CLASS);
        if(s==null || s.length()==0) {
            return Collections.emptyList() ;
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
        return additionalMainClassNames;
    }
}
