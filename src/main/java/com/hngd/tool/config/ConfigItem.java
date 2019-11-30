package com.hngd.tool.config;

import java.lang.reflect.InvocationTargetException;
import java.util.Properties;

import org.apache.commons.beanutils.BeanUtils;

import com.hngd.tool.exception.ScriptGenerationException;

import lombok.Data;

@Data
public class ConfigItem {

	/**
	 * 配置项名称
	 */
	private String  name;
	/**
	 * 是否必须
	 */
	private Boolean required;
	/**
	 * 默认值
	 */
	private String  defaultValue;
	
	/**
	 * 引用值,如果缺省的话
	 */
	private String  refIfAbsent;

	
	public ConfigItem(String name, Boolean required, String defaultValue, String refIfAbsent) {
		this.name = name;
		this.required = required;
		this.defaultValue = defaultValue;
		this.refIfAbsent = refIfAbsent;
	}
	public ConfigItem(String name, Boolean required, String defaultValue) {
		this(name, required, defaultValue, null);
	}
	public ConfigItem() {
		 
	}
	public ConfigValue loadValue(Properties properties) {
		Object value=properties.get(this.name);
		if(value==null && this.refIfAbsent!=null) {
			value=properties.get(this.refIfAbsent);
		}
		if(value==null && this.defaultValue!=null) {
			value=this.defaultValue;
		}
		if(this.required && value==null) {
			throw new ScriptGenerationException("缺少必要的配置项:"+this.name, null);
		}
		ConfigValue cv=new ConfigValue();
		try {
			BeanUtils.copyProperties(cv, this);
		} catch (IllegalAccessException | InvocationTargetException e) {
			throw new ScriptGenerationException("读取配置项:"+this.name+"出现错误", e);
		}
		cv.setValue(value);
		return cv;
	}
}
