package com.hngd.tool.utils;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
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
        if(!gitDir.exists() || !gitDir.isDirectory()) {
            log.info("The project is not a git repository");
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
        if(!CollectionUtils.isEmpty(tags) && StringUtils.isNotEmpty(buildInfo.getSourceId())) {
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
        FileUtils.write(new File(output,"build-info.properties"), buildInfo.toPropertiesString(), Constants.DEFAULT_CHARSET);
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
 
        public String toSimpleString() {
            
            SimpleDateFormat sdf=new SimpleDateFormat("yyyy/MM/dd HH:mm:ss:SSS");
            String buildTime=sdf.format(this.buildTime);
            StringBuilder sb=new StringBuilder();
            sb.append(version).append(LINE_DELIMITER);
            if(sourceId!=null) {
                sb.append(sourceId).append(LINE_DELIMITER);
            }else {
                sb.append("").append(LINE_DELIMITER);
            }
            sb.append(buildTime);
            return sb.toString();
        }
        
        
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