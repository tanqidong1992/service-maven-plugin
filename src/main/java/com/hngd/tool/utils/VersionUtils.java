package com.hngd.tool.utils;

import java.util.regex.Pattern;

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
