package com.hngd.tool.generator.impl;

import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class SystemdScriptGeneratorTest {

    public static void main(String[] args) throws IOException {
        new SystemdScriptGeneratorTest().test();
    }
    @Test
    public void test() throws IOException {
        SystemdScriptGenerator g=new SystemdScriptGenerator();
        Map<String, Object> params=new HashMap<>();
        params.put("serviceName","sn");
        params.put("serviceDescription","sd");
        params.put("after","a.service");
        params.put("wantedBy","wantedBy.target");
        g.generateServiceScript(new File("target"),params);

    }
}