package com.hngd.tool.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeroturnaround.exec.ProcessExecutor;
import org.zeroturnaround.exec.ProcessResult;
import org.zeroturnaround.exec.stream.slf4j.Slf4jStream;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.StandardCopyOption;

import java.nio.file.attribute.PosixFileAttributes;
import java.nio.file.attribute.PosixFilePermission;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeoutException;

public class ScriptCompiler {

    private static final Logger logger= LoggerFactory.getLogger(ScriptCompiler.class);
    private static final String SHC_PATH="/tools/shc";

    private File exec;

    public static ScriptCompiler getInstance(){

        ScriptCompiler scriptCompiler=new ScriptCompiler();
        scriptCompiler.extractCompiler();
        return scriptCompiler;
    }

    private void extractCompiler(){
        try {
            exec=File.createTempFile("shc",".sh");
            try(InputStream in=ScriptCompiler.class.getResourceAsStream(SHC_PATH)){
                Files.copy(in,exec.toPath(), StandardCopyOption.REPLACE_EXISTING);
            }
            PosixFileAttributes attributes=Files
                    .readAttributes(exec.toPath(), PosixFileAttributes.class, LinkOption.NOFOLLOW_LINKS);
            Set<PosixFilePermission> permissions= attributes.permissions();
            permissions.add(PosixFilePermission.OWNER_EXECUTE);
            Files.setPosixFilePermissions(exec.toPath(),permissions);
        } catch (IOException e) {
            logger.error("",e);
        }
    }

    public boolean compile(File source, File target) {
        List<String> cmds= Arrays.asList(exec.getAbsolutePath(),"-f",source.getAbsolutePath(),"-o",target.getAbsolutePath());
        ProcessResult pr= null;
        try {
            pr = new ProcessExecutor()
                    .command(cmds)
                    .readOutput(true)
                    .redirectError(Slf4jStream.of(logger).asDebug())
                    .execute();
            File tempFile=new File(source.getAbsolutePath()+".x.c");
            if(tempFile.exists()){
                tempFile.delete();
            }
        } catch (IOException | TimeoutException | InterruptedException e) {
            logger.error("",e);
        }
        return pr!=null && pr.getExitValue()==0;
    }


}
