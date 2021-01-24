package com.hngd.classloader;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarInputStream;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *工程类加载器
 * @author tqd
 *
 */
public class ProjectClassLoader extends ClassLoader{
 
    private final Map<String, byte[]> byteCache = new HashMap<>();
    private final Map<String, Class<?>> classes = new HashMap<>();
    private static final Logger logger=LoggerFactory.getLogger(ProjectClassLoader.class);
 
    public ProjectClassLoader(ClassLoader parent) {
        super(parent);
        Thread.currentThread().setContextClassLoader(this);
    }
    public void addClasspath(String...classpath) {
        for(String cp:classpath) {
            resolveClasspath(cp);
        }
    }

    private void resolveClasspath(String cp) {
        File file=new File(cp);
        if(!file.exists()) {
            logger.warn("the classpath:{} is not found",cp);
            return ;
        }
        if(file.isDirectory()) {
            doAddDirectory(file);
        }else if(file.getName().endsWith(".jar")) {
            doAddJar(file);
        }
        
    }

    private void doAddJar(File file) {
        JarFile jf=null;
        try {
            jf = new JarFile(file);
        } catch (IOException e) {
            logger.error("",e);
            return;
        }
        Enumeration<JarEntry> entries=jf.entries();
        while(entries.hasMoreElements()) {
            JarEntry entry=entries.nextElement();
            String entryName=entry.getName();
            if(entryName.endsWith(".class")) {
                doAddClassFileFromJar(jf,entry);
            }else if(entryName.endsWith(".jar")) {
                doAddJarFromJar(jf,entry);
            }
        }
    }

    private void doAddJarFromJar(JarFile jf, JarEntry entry) {
        try (InputStream jarStream=jf.getInputStream(entry)){
            doAddNestedJar(jarStream,entry);
        } catch (IOException e) {
            logger.error("",e);
        }
    }
    private void doAddNestedJar(InputStream jarStream, JarEntry entry) {
        try{
            JarInputStream jin=new JarInputStream(jarStream);
            JarEntry child=jin.getNextJarEntry();
            while(child!=null) {
                String entryName=child.getName();
                addClassIfClass(jin, entryName);
                if(entryName.endsWith(".jar")) {
                    doAddNestedJar(jin,entry);
                }
                child=jin.getNextJarEntry();
            }
        } catch (IOException e) {
            logger.error("",e);
        }
    }
    
    private void addClassIfClass(InputStream inputStream, String relativePath) throws IOException {
        if (relativePath.endsWith(".class")) {
            int len;
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            byte[] b = new byte[2048];
            while ((len = inputStream.read(b)) > 0) {
                out.write(b, 0, len);
            }
            out.close();
            byte[] classBytes = out.toByteArray();
            String className = relativePathToClassName(relativePath);
            //support Spring Boot Flat Jar
            if(className.startsWith("BOOT-INF.classes.")){
                className=className.replace("BOOT-INF.classes.", "");
            }
            byteCache.put(className, classBytes);
        }
    }

    private void doAddClassFileFromJar(JarFile jar, JarEntry entry) {
        String entryName=entry.getName();
        try (InputStream in=jar.getInputStream(entry)){
            addClassIfClass(in, entryName);
        } catch (IOException e) {
            logger.error("",e);
        }
    }

    private void doAddDirectory(File directory) {
        Collection<File> classFiles=FileUtils
            .listFiles(directory, new String[] {"class"}, true);
        classFiles.forEach(cf->this.doAddClassFile(directory,cf));
    }
    
    private void doAddClassFile(File classpath,File classFile) {
        String className=extractClassName(classpath, classFile);
        byte[] classByte=null;
        try {
            classByte=FileUtils.readFileToByteArray(classFile);
        } catch (IOException e) {
            logger.error("",e);
            return;
        }
        this.byteCache.put(className, classByte);
    }
    
    public static String extractClassName(File classpath,File classFile) {
        String fullPath=classFile.toURI().toString();
        String relativePath=fullPath.replace(classpath.toURI().toString(), "");
        String className=relativePathToClassName(relativePath);
        return className;
    }
    
    public static String relativePathToClassName(String relativePath) {
        String className=relativePath.substring(0, relativePath.lastIndexOf(".class")).replace("/", ".");
        return className;
    }
    
    @Override
    public Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        synchronized (getClassLoadingLock(name)) {
            Class<?> found =null;
            try {
                found=getParent().loadClass(name);
            }catch(Throwable e) {
                //Just ignore this exception
            }
            if (found != null) {
                return found;
            }
            found = findLoadedClass(name);
            if (found != null) {
                return found;
            }
            found = findLocalClass(name, resolve);
            if (found == null) {
                throw new ClassNotFoundException(name);
            }
            return found;
        }
    }
    
    public Class<?> findLocalClass(String className, boolean resolve) throws ClassNotFoundException {
        return getLoadedClass(className, resolve);
    }

    private Class<?> getLoadedClass(String className, boolean resolve) throws ClassNotFoundException{
        synchronized (getClassLoadingLock(className)) {
            Class<?> loadedClass = findLoadedClass(className);
            if (classes.containsKey(className)) {
                return classes.get(className);
            }
            if (byteCache.containsKey(className)) {
                definePackageForClass(className);
                byte[] classBytes = byteCache.get(className);
                if (loadedClass == null) {
                    //We got here without Exception, meaning class was filtered from proxying. Load normally:
                    try {
                        loadedClass = defineClass(className, classBytes, 0, classBytes.length,
                                this.getClass().getProtectionDomain());
                    } catch (NoClassDefFoundError | IncompatibleClassChangeError e) {
                        throw new ClassNotFoundException(className, e);
                    }
                }
                classes.put(className, loadedClass);
                if (resolve) {
                    resolveClass(loadedClass);
                }
                return loadedClass;
            } else {
                return null;
            }
        }
    }
    private void definePackageForClass(String className) {
        int i = className.lastIndexOf('.');
        if (i != -1) {
            String pkgname = className.substring(0, i);
            //Check if already defined:
            Package pkg = getPackage(pkgname);
            if (pkg == null) {
                definePackage(pkgname, null, null, null, null, null, null, null);
            }
        }
    }
    public List<String> listAllClass() {
        return byteCache.keySet()
            .stream()
            .collect(Collectors.toList());
    }
    
    public byte[] getClassByteCache(String name) {
        return  byteCache.get(name);
    }
     
}
