package com.hngd.tool.util;

import com.google.common.annotations.VisibleForTesting;
import com.hngd.tool.MojoParameters;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class SystemdUtils {

    public static String buildEnvironment(List<MojoParameters.Environment> envs){
        return envs.stream()
                .map(SystemdUtils::buildEnvironment)
                .collect(Collectors.joining(" "));
    }
    private static final Pattern pattern=Pattern.compile("\\S*\\s\\S*");
    @VisibleForTesting
    protected static String buildEnvironment(MojoParameters.Environment env){
        String value=env.getValue();
        Matcher matcher=pattern.matcher(value);
        String envStr=env.getName()+"="+env.getValue();
        if(matcher.find()){
            return "\""+envStr+"\"";
        }else {
            return envStr;
        }
    }
}
