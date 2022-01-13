package com.hngd.tool;

import java.io.File;

import org.apache.maven.plugins.annotations.Parameter;

public class MojoParameters {

    
    public static class ResourceDirectoryParameter{
        /**
         * source directory or file
         */
        @Parameter(required = true)
        private File from;
        /**
         * destination directory or file
         */
        @Parameter(required = true)
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

    public static class Environment{

        /**
         * name of env variable
         */
        @Parameter(required = true)
        private String name;
        /**
         * value of env variable
         */
        @Parameter
        private String value;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }
    }
}
