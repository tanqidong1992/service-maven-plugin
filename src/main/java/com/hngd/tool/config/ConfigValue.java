package com.hngd.tool.config;

import lombok.Data;
import lombok.EqualsAndHashCode;
@Data
@EqualsAndHashCode(callSuper = true)
public class ConfigValue extends ConfigItem{
	/**
	 * 值
	 */
	private Object value;
}
