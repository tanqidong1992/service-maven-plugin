package com.hngd.tool;

import java.io.File;

import org.apache.maven.plugins.annotations.Parameter;

import lombok.Data;

public class MojoParameters {

	@Data
	public static class ResourceDirectoryParameter{
		/**
		 *资源目录
		 */
		@Parameter
		private File from;
		/**
		 * 目标目录,相对于输出根目录
		 */
		@Parameter
		private String into;
	}
}
