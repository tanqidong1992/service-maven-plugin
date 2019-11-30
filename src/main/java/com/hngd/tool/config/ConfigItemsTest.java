package com.hngd.tool.config;

public class ConfigItemsTest {

	public static void main(String[] args) {
		
		ConfigItems.getAllConfigItems().stream()
		  .forEach(ci->{
			  System.out.println(ci);
		  });

	}

}
