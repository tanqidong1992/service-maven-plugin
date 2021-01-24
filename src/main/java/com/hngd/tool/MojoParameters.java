package com.hngd.tool;

import java.io.File;

import org.apache.maven.plugins.annotations.Parameter;

public class MojoParameters {

    
    public static class ResourceDirectoryParameter{
        /**
         * source directory or file
         */
        @Parameter
        private File from;
        /**
         * destination directory or file
         */
        @Parameter
        private String into;
        
        public File getFrom() {
            return from;
        }
        public void setFrom(File from) {
            this.from = from;
        }
        
        public String getInto() {
            return into;
        }
        public void setInto(String into) {
            this.into = into;
        }
        
        
    }
}
