package com.hngd.tool.utils;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.apache.commons.collections.CollectionUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.lib.RepositoryBuilder;

import com.google.common.io.Files;
import com.hngd.tool.constant.Constants;

import lombok.extern.slf4j.Slf4j;
@Slf4j
public class BuildInfoUtils {

	public static void generateBuildInfo(File projectBaseDir,File output) throws IOException {
		
		File gitDir=new File(projectBaseDir,".git");
		if(!gitDir.exists() || !gitDir.isDirectory()) {
			log.info("The project is not a git repository");
			return ;
		}
		Repository repository=new  RepositoryBuilder()
				.setGitDir(gitDir)
				.build();
		 
		Git git=Git.wrap(repository);
		String buildId="0.0.1";
		try {
			buildId=git.describe().call();
		} catch (GitAPIException e) {
			log.error("",e);
			 
		}
		String description=buildId;
		String lastVersion=description;
		try {
			List<Ref> tags=git.tagList().call();
			if(!CollectionUtils.isEmpty(tags)) {
				Optional<String> lastTagName=tags.stream()
					    .map(tag->tag.getName().replace("refs/tags/", ""))
					    .filter(tagName->{
					    	return description.startsWith(tagName);
					    	})
					    .findFirst();
					if(lastTagName.isEmpty()) {
						lastVersion=buildId;
					}else {
						lastVersion=lastTagName.get();
					}
			}else {
				lastVersion=buildId;
			}
		} catch (GitAPIException e) {
			log.error("",e);
		}
		
		SimpleDateFormat sdf=new SimpleDateFormat("yyyy/MM/dd HH:mm:ss:SSS");
		String buildTime=sdf.format(new Date());
		StringBuilder sb=new StringBuilder();
		sb.append(lastVersion).append("\n");
		sb.append(buildId).append("\n");
		sb.append(buildTime);
		Files.write(sb.toString(), new File(output,"build-info"), Constants.DEFAULT_CHARSET);
	}
}
