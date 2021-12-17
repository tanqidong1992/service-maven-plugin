package com.hngd.tool.util;

import org.junit.jupiter.api.Test;

import java.io.File;

class ScriptCompilerTest {

    public static void main(String[] args) {
        new ScriptCompilerTest().test();
    }

    @Test
    public void test(){
        File source=new File("src/test/shell/hello.sh");
        File target=new File("target/hello.sh");
        ScriptCompiler.getInstance().compile(source,target);
    }
}