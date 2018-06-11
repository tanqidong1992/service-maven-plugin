#### 简介

##### 实现功能

这是一个maven插件项目,用于生成windows下的java程序包，程序包包含，可行性jar文件，依赖的第三方jar文件，程序运行依赖的java运行时，数据，生成的运行脚本（支持NT服务方式）。

##### 使用

1. 插件坐标

   ```xml
   <plugin>
   				<groupId>com.hngd.tool</groupId>
   				<artifactId>maven-winpackage-plugin</artifactId>
   				<version>0.0.1</version>
   				<executions>
   					<execution>
   						<goals>
   							<goal>win-package</goal>
   						</goals>
   						<phase>package</phase>
   						<configuration>
   						    <jreDirectory>C:\\Program Files\\Java\\jre1.8.0_121</jreDirectory>
   						    <scriptConfigFile>${basedir}/package-config/punchSystem.properties</scriptConfigFile>
   							<outputDirectory>${project.build.directory}/winpackage</outputDirectory>
   							<dependencyDirectory>${project.build.directory}/libs</dependencyDirectory>
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

   插件远程仓库地址

   ```xml
   <repository>
     <id>hnoe-tfs-maven-plugin</id>
     <url>http://hnoe-tfs:8080/tfs/DefaultCollection/_packaging/maven-plugin/maven/v1</url>
     <releases>
       <enabled>true</enabled>
     </releases>
     <snapshots>
       <enabled>true</enabled>
     </snapshots>
   </repository>
   ```

2. 配置文件说明

   ```properties
   #NT服务名称
   serviceName=HnPunchSystem        
   #NT服务显示名称
   serviceDisplayName=HnPunchSystem
   #Java程序入口类
   mainClass=com.hngd.NtServiceMain
   #NT服务启动时，调用Java方法
   startMethod=onStart     
   #NT服务停止时，调用Java方法
   stopMethod=onStop
   #是否生成NT服务，安装，启动，停止，卸载脚本
   supportService=true
   #更多参数设置将在后续支持
   ```

3. 示例项目参考

   1. [考勤系统](http://192.168.0.143:8080/tfs/DefaultCollection/TfsDemo/TfsDemo%20%E5%9B%A2%E9%98%9F/_git/PunchSystem)

##### 编译环境

1. Oracle JDK 1.8
2. Apache Maven 3.3.3.

##### 编译

```shell
mvn package //打包
mvn install //安装到本地
mvn deploy //发布程序包
```

##### 相关资料

1. 如何发布包到TFS包管理器
2. maven 插件开发