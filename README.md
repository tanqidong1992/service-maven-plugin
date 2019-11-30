# 简介

## 实现功能

这是一个maven插件项目,主要用于生成windows下支持以服务形式运行java程序的脚本,包括服务安装,启动,停止,卸载的脚本。支持Spring boot项目.

## 使用

1. 插件坐标

   ```xml
   
                <plugin>
   				<groupId>com.hngd.tool</groupId>
   				<artifactId>service-maven-plugin</artifactId>
   				<version>0.0.5-SNAPSHOT</version>
   				<executions>
   					<execution>
   						<goals>
   							<goal>win-package</goal>
   						</goals>
   						<phase>package</phase>
   						<configuration>
   						    <jreDirectory>C:\\Program Files\\Java\\jre1.8.0_121</jreDirectory>
   						    <scriptConfigFile>${basedir}/package-config/package.properties</scriptConfigFile>
   							<outputDirectory>${project.build.directory}/${artifactId}</outputDirectory>
   
   						    <configAndDataDirectories>
   						    <param>${basedir}/config</param>
   						    <param>${basedir}/data</param>
   						    <param>${basedir}/tools</param>
   						    </configAndDataDirectories>
   						</configuration>
   					</execution>
   				</executions>
   			</plugin>
   ```
   
2. 配置文件说明(${basedir}/package-config/package.properties)

   ```properties
   #NT服务名称
   serviceName=demo-service        
   #NT服务显示名称
   serviceDisplayName=demo-service
   #NT服务描述内容
   serviceDescription=测试服务
   #Java程序入口类
   mainClass=com.hngd.NtServiceMain
   #NT服务启动时，调用Java方法
   startMethod=onStart     
   #NT服务停止时，调用Java方法
   stopMethod=onStop
   #是否生成NT服务的安装，启动，停止，卸载脚本
   supportService=true
   #java虚拟机内存配置
   jvmMs=512m
   jvmMx=1024m
   #更多参数设置将在后续支持
   ```


## 编译环境

1. Oracle JDK 1.8.
2. Apache Maven 3.3.3及以上.

## 编译

```shell
mvn package //打包
mvn install //安装到本地
mvn deploy //发布程序包
```

## 相关资料

1. [maven 插件开发](https://maven.apache.org/plugin-developers/index.html)
2. [commons-daemon]( http://commons.apache.org/proper/commons-daemon/procrun.html )