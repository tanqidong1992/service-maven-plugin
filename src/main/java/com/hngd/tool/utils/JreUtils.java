package com.hngd.tool.utils;

import org.apache.commons.lang3.JavaVersion;

public class JreUtils {

    private static final String JAVA_HOME = "java.home";
    public static String getDefaultJrePath(){
        String defaultJrePath=System.getProperty(JAVA_HOME);
        return defaultJrePath;
    }
    
    public static boolean atLeastJava11() {
        return JavaVersion.JAVA_RECENT.atLeast(JavaVersion.JAVA_11);
    }
    
    public static final String OS_NAME_LINUX = "Linux";
    public static final String KEY_OS_NAME   = "os.name";
    private static String OS = System.getProperty(KEY_OS_NAME).toLowerCase();
    public static boolean isLinux(){
        return OS.indexOf("linux")>=0;
    }
    public static boolean isWindows(){
        return OS.indexOf("windows")>=0;
    }

}
