package com.hngd.tool.util;

import java.io.File;
import java.io.IOException;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hngd.tool.classloader.ProjectClassLoader;
import com.hngd.tool.constant.Constants;

public class MainClassDetector {
    
    private static Logger logger=LoggerFactory.getLogger(MainClassDetector.class);

    public static Optional<ClassWeight> findTheMostAppropriateMainClass(String mainJarFilePath) throws IOException{
        ProjectClassLoader loader=new ProjectClassLoader(MainClassDetector.class.getClassLoader());
        File file=new File(mainJarFilePath);
        loader.addClasspath(file.getAbsolutePath());
        List<String> classNames=loader.listAllClass();
        Optional<ClassWeight> optionalMaxWeightClass=classNames.stream()
          .parallel()
          .map(name->loader.getClassByteCache(name))
          .map(clazz->caculateWeight(clazz))
          .filter(w->w.weight>0)
          .max(Comparator.comparing(w->w.weight));
        return optionalMaxWeightClass;
    }

    public static ClassWeight caculateWeight(byte[] data) {

        ClassWeight classWeight = new ClassWeight();
        ClassReader classReader = new ClassReader(data);
        ClassVisitor classVisitor = new ClassVisitor(Opcodes.ASM9) {
            @Override
            public MethodVisitor visitMethod(int access, String name, String descriptor, String signature,
                    String[] exceptions) {
                //public static void *(String[]args)
                if((Opcodes.ACC_STATIC&access)>0 && (Opcodes.ACC_PUBLIC&access)>0 &&"([Ljava/lang/String;)V".equals(descriptor)) {
                    if (Constants.DEFAULT_MAIN_METHOD_NAME.equals(name)) {
                        classWeight.weight++;
                    } else if (Constants.DEFAULT_ON_START_METHOD_NAME.equals(name)) {
                        classWeight.weight++;
                    } else if (Constants.DEFAULT_ON_STOP_METHOD_NAME.equals(name)) {
                        classWeight.weight++;
                    }
                }
                return super.visitMethod(access, name, descriptor, signature, exceptions);
            }
        };
        classReader.accept(classVisitor, ClassReader.EXPAND_FRAMES);
        classWeight.name = classReader.getClassName().replace("/", ".");
        return classWeight;
    }
}