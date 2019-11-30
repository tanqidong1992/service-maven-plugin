# 简介

## 实现功能

这是一个maven插件项目,主要用于生成windows下支持以服务形式运行java程序的脚本,包括服务安装,启动,停止,卸载的脚本.支持Spring Boot项目.

## 使用

1. 插件坐标

   ```xml
       <plugin>
   	    <groupId>com.hngd.tool</groupId>
   		<artifactId>service-maven-plugin</artifactId>
   		<version>0.0.6-SNAPSHOT</version>
   		<executions>
   		    <execution>
                   <goals>
   				    <goal>win-package</goal>
   				</goals>
   				<phase>package</phase>
   				<configuration>
   				    <resourceDirectories>
   						<resourceDirectory>config</resourceDirectory>
   						<resourceDirectory>data</resourceDirectory>
   					</resourceDirectories>
   				</configuration>
   		    </execution>
   	    </executions>
       </plugin>
   ```
   
2. 配置文件说明(配置文件不是必须的)

   ```properties
   #NT服务名称,如果为空,插件会取POM文件中的${project.name}或者${project.artifactId}作为此项的值
   serviceName=demo-service        
   #NT服务显示名称,如果此项为空,,插件会取serviceName作为此项的值
   serviceDisplayName=demo-service
   #NT服务描述内容,如果为空,插件会取POM文件中的${project.description}或者serviceName作为此项的值
   serviceDescription=测试服务
   #Java程序启动类,如果为空,那么插件回去找一个寻找一个合适的类作为此项的值
   mainClass=com.hngd.NtServiceMain
   #NT服务启动时，调用Java方法,默认值为onStart
   startMethod=onStart     
   #NT服务停止时，调用Java方法,默认值为onStop
   stopMethod=onStop
   #是否生成NT服务的安装，启动，停止，卸载脚本,默认为false,
   #如果存在合适的入口类(同时存在main,onStart,onStop方法),那么此项值为true
   supportService=true
   #java虚拟机内存配置
   jvmMs=512m
   jvmMx=1024m
   #服务启动方式,可取值为delayed(延迟启动), auto(自动启动) or manual(手动启动),默认为manual
   startup=auto
   #更多参数设置将在后续支持
   ```
   
   如果你的项目中存在一个以下形式的类,那么配置文件是可以省略的
   
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
   
3. 执行插件

   ```shell
   mvn clean package #service:win-package -DskipTests
   ```

## 输出目录结构

```shell
.
├── config                              #程序资源目录
├── data                                #程序资源目录
├── install.bat                         #NT服务安装脚本
├── jre                                 #JRE运行时
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

1. [maven 插件开发](https://maven.apache.org/plugin-developers/index.html)
2. [commons-daemon]( http://commons.apache.org/proper/commons-daemon/procrun.html )