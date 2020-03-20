package com.hngd.tool.generator;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import org.beetl.core.exception.BeetlException;

public interface ScriptGenerator {

	public void generateDaemonScript(File workdir, Map<String, Object> context) throws BeetlException, IOException;
	public void generateConsoleScript(File workdir, Map<String, Object> context);
	
}
