package com.hngd.tool.git;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.beanutils.BeanUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.Status;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.lib.RepositoryBuilder;

import com.hngd.tool.utils.BuildInfoUtils;


public class JGitTest {

	public static void main(String[] args) throws IOException {
		 
		File gitDir=new File("W:\\company\\hnoss\\hnoss-mini-helper");
		BuildInfoUtils.generateBuildInfo(gitDir, gitDir);

	}

}
