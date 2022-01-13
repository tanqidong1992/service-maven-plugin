package com.hngd.tool.util;

import com.google.common.annotations.VisibleForTesting;
import com.hngd.tool.MojoParameters;

import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class SystemdUtils {

    public static String buildEnvironment(List<MojoParameters.Environment> envs){
        return envs.stream()
                .map(SystemdUtils::buildEnvironment)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.joining(" "));
    }

    private static final Pattern pattern=Pattern.compile("\\S*\\s\\S*");

    @VisibleForTesting
    protected static Optional<String> buildEnvironment(MojoParameters.Environment env){
        String value=env.getValue();
        if(value==null || env.getName()==null){
            return Optional.empty();
        }
        Matcher matcher=pattern.matcher(value);
        String envStr=env.getName()+"="+env.getValue();
        if(matcher.find()){
            return Optional.of("\""+envStr+"\"");
        }else {
            return Optional.of(envStr);
        }
    }
}
