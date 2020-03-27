# Maven Windows NT服务打包插件

## 简述

这是一个maven插件项目,主要用于生成Windows下支持以服务形式运行java程序的脚本,包括服务安装,启动,停止,卸载的脚本.

## 主要功能

- 零配置,自动检测入口类,根据Maven工程信息生成Windows NT服务信息.
- 支持生成Windows NT服务的安装,启动,停止,卸载脚本.
- 支持生成控制台启动脚本.
- 支持Spring Boot项目.
- 对于Java 11支持定制Java运行镜像.

## 使用

### 插件坐标

```xml
    <plugin>
	    <groupId>com.hngd.tool</groupId>
		<artifactId>service-maven-plugin</artifactId>
		<version>0.0.8-SNAPSHOT</version>
		<executions>
		    <execution>
                <goals>
				    <goal>win-package</goal>
				</goals>
				<phase>package</phase>
				<configuration>
				    <resources>
						<param>${basedir}/data</param>
						<param>${basedir}/config</param>
						<param>${basedir}/test-data</param>
					</resources>
				</configuration>
		    </execution>
	    </executions>
    </plugin>
```

### 插件配置项说明

- jreDirectory,可选配置项,Jre目录,配置后,插件将会复制此目录到输出目录,如果此项为空,那么插件会将执行插件的Jre复制到输出目录.
- customRuntimeImage,可选配置项,默认为false,如果设置为true并且配置项jreDirectory未设置,那么插件将使用jdeps,jlink自定义Java运行时并输出到输出目录下的jre子目录中.
- targetJreVersion,可选配置项,当程序依赖了多发行版的Jar时,指定处理多发行版 jar 文件时的版本,应为大于等于 9.
- compressLevel,可选配置项,指定自定义Java运行时的压缩级别,可取值0,1,2;0: No compression,1: Constant string sharing,2: ZIP
- scriptConfigFile,可选配置项,指定打包配置文件路径
- outputDirectory,可选配置项,指定打包输出目录,默认值为${project.build.directory}/${project.artifactId}
- resources,可选配置项,资源目录或者文件,配置后将复制到输出目录.

### 配置文件说明

```properties
#NT服务名称,如果为空,插件会取POM文件中的${project.name}或者${project.artifactId}作为此项的值
serviceName=demo-service        
#NT服务显示名称,如果此项为空,,插件会取serviceName作为此项的值
serviceDisplayName=demo-service
#NT服务描述内容,如果为空,插件会取POM文件中的${project.description}或者serviceName作为此项的值
serviceDescription=测试服务
#Java程序启动类,如果为空,那么插件会去寻找一个合适的类作为此项的值
mainClass=com.hngd.NtServiceMain
#NT服务启动时，调用的Java方法,默认值为onStart
startMethod=onStart     
#NT服务停止时，调用的Java方法,默认值为onStop
stopMethod=onStop
#是否生成NT服务的安装，启动，停止，卸载脚本,默认为false,
#如果存在合适的入口类(同时存在main,onStart,onStop方法),那么此项值默认为true
supportService=true
#java虚拟机内存配置
jvmMs=512m
jvmMx=1024m
#服务启动方式,可取值为delayed(延迟启动), auto(自动启动) or manual(手动启动),默认为manual
startup=auto
#配置生成其他主入口类控制台启动脚本,生成文件名称为run.${类名}.bat
additionalMainClass=com.hngd.Main1,com.hngd.Main2,com.hngd.Main3
#更多参数设置将在后续支持
```

**配置文件不是必须的**,如果你的项目中存在一个以下形式的类,那么配置文件是可以省略的

```java
public class EntryClass{
    public static void main(String[] args){
        //TODO,这个方法是必须的,
    }
    public static void onStart(String[] args){
        //TODO,这个方法是可选的,如果要生成NT服务相关操作脚本,那么这个方法是必须的
    }
    public static void onStop(String[] args){
        //TODO,这个方法是可选的,如果要生成NT服务相关操作脚本,那么这个方法是必须的
    }
}
```

### 执行插件

```shell
mvn clean package #service:win-package -DskipTests
```

## 输出目录结构

```shell
.
├── config                              #程序资源目录
├── data                                #程序资源目录
├── install.bat                         #NT服务安装脚本
├── jre                                 #Java运行时
├── libs                                #依赖的第三方库
├── logs                                #日志文件目录
├── ntservice-demo-0.0.1-SNAPSHOT.jar   #主jar文件
├── prunsrv.exe                         #Apache Commons Daemon可执行文件
├── run.bat                             #控制台启动脚本
├── start.bat                           #NT服务启动脚本
├── stop.bat                            #NT服务停止脚本
└── uninstall.bat                       #NT服务卸载脚本
```

## 编译环境

1. Oracle JDK 1.8.
2. Apache Maven 3.3.3及以上.

## 编译

```shell
mvn package //打包
mvn install //安装
mvn deploy //发布
```

## 相关资料

1. [Maven 插件开发](https://maven.apache.org/plugin-developers/index.html)
2. [commons-daemon]( http://commons.apache.org/proper/commons-daemon/procrun.html )
3. [jlink]( https://docs.oracle.com/en/java/javase/14/docs/specs/man/jlink.html )
4. [jdeps]( https://docs.oracle.com/en/java/javase/14/docs/specs/man/jlink.html )