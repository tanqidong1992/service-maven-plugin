package com.hngd.tool;

import java.io.File;

import org.apache.maven.plugins.annotations.Parameter;

import lombok.Data;

public class MojoParameters {

	@Data
	public static class ResourceDirectoryParameter{
		/**
		 * source directory or file
		 */
		@Parameter
		private File from;
		/**
		 * destination directory or file
		 */
		@Parameter
		private String into;
	}
}
