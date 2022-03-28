package com.hngd.tool.mojo;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collection;

import org.apache.commons.io.FileUtils;
import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.apache.maven.shared.invoker.*;
import org.codehaus.plexus.util.ReaderFactory;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class MojoTest{

	private Path tmp;
	File pomFile = new File("src/test/resources/dependency-test/pom.xml" );
	@BeforeEach
	public void setup() throws IOException {
		tmp= Files.createTempDirectory("test-pom");
		FileUtils.copyFileToDirectory(pomFile,tmp.toFile());
	}

	@AfterEach
	public void cleanup() throws IOException {
		FileUtils.deleteDirectory(tmp.toFile());
	}

	@Test
	public void readModelTest() throws IOException, XmlPullParserException {
		Model model=new MavenXpp3Reader().read(ReaderFactory.newXmlReader( pomFile ));
		model.getDependencies().forEach(s->{
			System.out.println(s.getScope());
		});
	}

	@Test
	public void testMojo() throws Exception {

		InvocationRequest request=new DefaultInvocationRequest();
		//request.setPomFile(testPom);
		request.setGoals(Arrays.asList("package"));
		request.setBaseDirectory(tmp.toFile());
		Invoker invoker=new DefaultInvoker();
		invoker.setWorkingDirectory(tmp.toFile());
		InvocationResult result=invoker.execute(request);
		System.out.println(tmp);
		Collection<File> libs=FileUtils.listFiles(Paths.get(tmp.toString(),"target","test","libs").toFile(),null,false);
		Assertions.assertEquals(2,libs.size());

	}
}
