package com.hngd.tool.mojo;

import java.io.File;

import com.hngd.tool.DependencyTestMojo;
import org.apache.maven.plugin.testing.AbstractMojoTestCase;
import org.apache.maven.project.*;
import org.eclipse.aether.RepositorySystemSession;


public class MojoTest extends AbstractMojoTestCase{

	@Override
	protected void setUp() throws Exception {
		// TODO Auto-generated method stub
		super.setUp();
	}
	
	public void testMojo() throws Exception {
        File testPom = new File(
                "src/test/resources/dependency-test/pom.xml" );

		DependencyTestMojo mojo = (DependencyTestMojo) lookupMojo( "dependency-test", testPom );
        lookup(RepositorySystemSession.class);
		ProjectBuilder dpb=lookup(ProjectBuilder.class);
		DefaultProjectBuildingRequest pbr=new DefaultProjectBuildingRequest();
		ProjectBuildingResult pbs=dpb.build(testPom, pbr);
        pbs.getProject();
        mojo.setProject(pbs.getProject());
        assertNotNull( mojo );
        mojo.execute();
	}
}
