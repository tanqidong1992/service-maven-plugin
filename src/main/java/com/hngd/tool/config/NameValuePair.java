package com.hngd.tool.config;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class NameValuePair extends ConfigItem{
    /**
     * 值
     */
    private Object value;
 
    public boolean isValuePresent() {
        return value!=null;
    }
}
