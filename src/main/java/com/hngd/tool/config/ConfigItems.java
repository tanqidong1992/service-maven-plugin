package com.hngd.tool.config;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import com.hngd.tool.constant.Constants;

public class ConfigItems {

    public static final String INNER_PROJECT_NAME="#projectName";
    public static final String INNER_PROJECT_DESCRIPTION="#projectDescription";
    public static final String INNER_PROJECT_MAIN_CLASS="#projectMainClass";
    public static final String INNER_PROJECT_MAIN_CLASS_SUPPORT_SERVICE="#projectMainClassSupportService";
    public static final String KEY_SUPPORT_SERVICE = "supportService";
    public static final String KEY_SERVICE_DESCRIPTION = "serviceDescription";
    public static final String KEY_SERVICE_DISPLAY_NAME = "serviceDisplayName";
    public static final String KEY_SERVICE_NAME = "serviceName";
    public static final String KEY_JVM_MS = "jvmMs";
    public static final String KEY_JVM_MX = "jvmMx";
    public static final String KEY_JAVA_RUN_OPTIONS = "javaRunOptions";
    public static final String KEY_MAIN_CLASS = "mainClass";
    public static final String KEY_ADDITIONAL_MAIN_CLASS = "additionalMainClass";
    public static final String KEY_START_METHOD = "startMethod";
    public static final String KEY_STOP_METHOD = "stopMethod";
    
    public static final String KEY_STARTUP = "startup";
    /**
     * 服务启动方式,默认为manual,Service startup mode can be either delayed, auto or manual
     */
    public static ConfigItem STARTUP = new ConfigItem(KEY_STARTUP, false, "manual");
    public static ConfigItem START_METHOD = new ConfigItem(KEY_START_METHOD, false, Constants.DEFAULT_ON_START_METHOD_NAME);
    public static ConfigItem STOP_METHOD = new ConfigItem(KEY_STOP_METHOD, false, Constants.DEFAULT_ON_STOP_METHOD_NAME);
    public static ConfigItem MAIN_CLASS = new ConfigItem(KEY_MAIN_CLASS, false, null,INNER_PROJECT_MAIN_CLASS);
    
    public static ConfigItem ADDITIONAL_MAIN_CLASS = new ConfigItem(KEY_ADDITIONAL_MAIN_CLASS, false, null);
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
    public static ConfigItem SERVICE_NAME = new ConfigItem(KEY_SERVICE_NAME, false, null,INNER_PROJECT_NAME);
 
    public static List<ConfigItem> getAllConfigItems(){
        Field[]  fields=ConfigItems.class.getFields();
        if(fields==null) {
            return Collections.emptyList();
        }
        List<ConfigItem> all=new LinkedList<>();
        for(Field field:fields) {
            Class<?> type=field.getType();
            if(!ConfigItem.class.equals(type)) {
                continue;
            }
            ConfigItem ci;
            try {
                field.setAccessible(true);
                ci = (ConfigItem) field.get(ConfigItems.class);
            } catch (IllegalArgumentException | IllegalAccessException e) {
                throw new RuntimeException("", e);
            }
            all.add(ci);
        }
        return all;
    }
}
