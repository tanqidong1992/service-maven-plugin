package com.hngd.tool.util;

public class JreUtils {

    private static final String JAVA_HOME = "java.home";
    public static String getDefaultJrePath(){
        String defaultJrePath=System.getProperty(JAVA_HOME);
        return defaultJrePath;
    }
    /**
     * 11.0.11+
     * @return
     */
    public static boolean atLeastJava11() {
        String currentJavaVersion=System.getProperty("java.version");
        String[] versionParts =currentJavaVersion.split("\\.");
        int majorVersion= Integer.parseInt(versionParts[0]);
        if(majorVersion> 11){
            return true;
        }
        if(majorVersion==11){
            int minorVersion= Integer.parseInt(versionParts[1]);
            if(minorVersion>0){
                return true;
            }else{
                int patchVersion=Integer.valueOf(versionParts[2]);
                return patchVersion >= 11;
            }
        }
        return false;
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
