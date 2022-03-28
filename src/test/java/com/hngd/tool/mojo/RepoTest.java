package com.hngd.tool.mojo;

import java.io.File;
import java.net.MalformedURLException;

import org.apache.maven.artifact.DefaultArtifact;
import org.apache.maven.artifact.handler.ArtifactHandler;
import org.apache.maven.artifact.handler.manager.ArtifactHandlerManager;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.repository.ArtifactRepositoryFactory;
import org.apache.maven.artifact.repository.layout.DefaultRepositoryLayout;
import org.apache.maven.artifact.resolver.ArtifactResolutionRequest;
import org.apache.maven.artifact.resolver.ArtifactResolutionResult;
import org.apache.maven.artifact.resolver.ArtifactResolver;
import org.apache.maven.plugin.testing.MojoRule;

import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

@Deprecated
public class RepoTest {

    @Test
    public void test() throws ComponentLookupException{
        MojoRule rule=new MojoRule();
        ArtifactRepositoryFactory artifactRepositoryFactory=
        		rule.lookup(ArtifactRepositoryFactory.class);
        
        String url=null;
        try {
            url=new File("/data/app/maven-repo").toURI().toURL().toString();
        }catch(MalformedURLException e) {
        	e.printStackTrace();
        }
        ArtifactRepository artifactRepository=artifactRepositoryFactory.createArtifactRepository("test",
        		url, 
        		new DefaultRepositoryLayout(), 
        		null, null);
        ArtifactResolutionRequest arr=new ArtifactResolutionRequest();
        arr.setLocalRepository(artifactRepository);
        ArtifactResolver artifactResolver=rule.lookup(ArtifactResolver.class);
        ArtifactHandler jarHandler=rule.lookup(ArtifactHandlerManager.class)
        		.getArtifactHandler("jar");
		/**
         <dependency>
              <groupId>org.mockito</groupId>
              <artifactId>mockito-core</artifactId>
              <version>3.3.0</version>
              <scope>test</scope>
         </dependency>
         */
        DefaultArtifact da=
        		new DefaultArtifact("org.mockito", "jmockito-core",
        				"3.3.0", null, "jar", null, jarHandler);
        arr.setArtifact(da);
        ArtifactResolutionResult ars=artifactResolver.resolve(arr);
        Assertions.assertTrue(ars.getArtifacts().size()==1);
        
        
        
    }
}
