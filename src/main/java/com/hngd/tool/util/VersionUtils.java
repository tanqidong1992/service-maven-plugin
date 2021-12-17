package com.hngd.tool.util;

public class VersionUtils {
    /**
     * https://docs.fedoraproject.org/en-US/packaging-guidelines/Versioning/
     * @param originVersion
     * @return
     */
    public static String fixToRpmVersion(String originVersion){

        return originVersion.replace("-",".");
    }
}
