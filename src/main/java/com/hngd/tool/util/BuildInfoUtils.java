package com.hngd.tool.util;

import java.io.File;
import java.io.IOException;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.apache.commons.io.FileUtils;
import org.apache.maven.shared.utils.StringUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.lib.RepositoryBuilder;

import com.hngd.tool.constant.Constants;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class BuildInfoUtils {

    public static final String DEFAULT_VERSION="0.0.1";
    public static void generateBuildInfo(File projectBaseDir,File output) throws IOException {
        File gitDir=new File(projectBaseDir,".git");

        //git submodules
        if(gitDir.isFile()){
            //gitdir: ../.git/modules/test
            String s=FileUtils.readFileToString(gitDir, StandardCharsets.UTF_8.name());
            String relativePath=s.trim().replace("gitdir: ","");
            gitDir=new File(projectBaseDir,relativePath);
        }

        if(!gitDir.exists() || !gitDir.isDirectory()) {
            log.warn("The project is not a git repository:"+gitDir.getAbsolutePath());
            return ;
        }
        Repository repository=new  RepositoryBuilder()
                .setGitDir(gitDir)
                .build();
        BuildInfo buildInfo=new BuildInfo();
        buildInfo.setBuildTime(new Date());
        Git git=Git.wrap(repository);
        try {
            String buildId=git.describe().call();
            buildInfo.setSourceId(buildId);
        } catch (GitAPIException e) {
            log.error("",e);
        }
        List<Ref> tags=null;
        try {
            tags=git.tagList().call();
        } catch (GitAPIException e) {
            log.error("",e);
        }
        if(tags!=null && StringUtils.isNotEmpty(buildInfo.getSourceId())) {
            String description=buildInfo.getSourceId();
            Optional<String> lastTagName=tags.stream()
                    .map(tag->tag.getName().replace("refs/tags/", ""))
                    .filter(tagName->description.startsWith(tagName))
                    .findFirst();
                if(lastTagName.isPresent()) {
                    buildInfo.setVersion(lastTagName.get());
                }
        }
        if(StringUtils.isEmpty(buildInfo.getVersion())) {
            buildInfo.setVersion(DEFAULT_VERSION);
        }
        Files.write(new File(output,"build-info.properties").toPath(),
                buildInfo.toPropertiesString().getBytes(Constants.DEFAULT_CHARSET), StandardOpenOption.CREATE);
        log.info("write to "+new File(output,"build-info.properties").getAbsolutePath());
    }
    
    @Data
    public static class BuildInfo{
        private static final String LINE_DELIMITER="\r\n";
        /**
         * 版本号
         */
        private String version;
        /**
         * 构建源码id,取自git describe
         */
        private String sourceId;
        /**
         * 构建时间
         */
        private Date buildTime;

        public String toPropertiesString() {
            
            SimpleDateFormat sdf=new SimpleDateFormat("yyyy/MM/dd HH:mm:ss:SSSXXX");
            String buildTime=sdf.format(this.buildTime);
            StringBuilder sb=new StringBuilder();
            sb.append("version=").append(version).append(LINE_DELIMITER);
            if(sourceId!=null) {
                sb.append("sourceId=").append(sourceId).append(LINE_DELIMITER);
            }
            sb.append("buildTime=").append(buildTime);
            return sb.toString();
        }
    }
}