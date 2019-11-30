package com.hngd.tool.config;

import java.util.Arrays;
import java.util.List;

public class ConfigItems {

	public static final String KEY_SUPPORT_SERVICE = "supportService";
	public static final String KEY_SERVICE_DESCRIPTION = "serviceDescription";
	public static final String KEY_SERVICE_DISPLAY_NAME = "serviceDisplayName";
	public static final String KEY_SERVICE_NAME = "serviceName";
	public static final String KEY_JVM_MS = "jvmMs";
	public static final String KEY_JVM_MX = "jvmMx";
	public static final String KEY_JAVA_RUN_OPTIONS = "javaRunOptions";
	public static final String KEY_MAIN_CLASS = "mainClass";
	
	public static final String KEY_START_METHOD = "startMethod";
	public static final String KEY_STOP_METHOD = "stopMethod";
	
	public static ConfigItem START_METHOD = new ConfigItem(KEY_START_METHOD, false, null);
	public static ConfigItem STOP_METHOD = new ConfigItem(KEY_STOP_METHOD, false, null);
	public static ConfigItem MAIN_CLASS = new ConfigItem(KEY_MAIN_CLASS, false, null);
	/**
	 * 是否生成服务操作脚本
	 */
	public static ConfigItem SUPPORT_SERVICE = new ConfigItem(KEY_SUPPORT_SERVICE, false, "false");
    /**
     * java启动参数
     */
	public static ConfigItem JAVA_RUN_OPTIONS = new ConfigItem(KEY_JAVA_RUN_OPTIONS, false, null);
    /**
     * 虚拟机堆内存最小值
     */
	public static ConfigItem JVM_MS = new ConfigItem(KEY_JVM_MS, false, null);
	/**
	 * 虚拟机堆内存最大值
	 */
	public static ConfigItem JVM_MX = new ConfigItem(KEY_JVM_MX, false, null);
    /**
     * 服务描述
     */
	public static ConfigItem SERVICE_DESCRIPTION = new ConfigItem(KEY_SERVICE_DESCRIPTION, false, null,
			KEY_SERVICE_NAME);
	/**
	 * 服务显示名称
	 */
	public static ConfigItem DISPLAY_NAME = new ConfigItem(KEY_SERVICE_DISPLAY_NAME, false, null, KEY_SERVICE_NAME);
	/**
	 * 服务名称,如果supportService为true,那么此项一定不能为空
	 */
	public static ConfigItem SERVICE_NAME = new ConfigItem(KEY_SERVICE_NAME, false, null);

	public static List<ConfigItem> ALL = Arrays.asList(
			SUPPORT_SERVICE,
			JAVA_RUN_OPTIONS,
			JVM_MS,
			JVM_MX,
			SERVICE_DESCRIPTION,
			DISPLAY_NAME,
			SERVICE_NAME,
			MAIN_CLASS,
			START_METHOD,
			STOP_METHOD
	);
}
