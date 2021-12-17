package com.hngd.tool.util;

class JreUtilsTest {

    public static void main(String[] args) {

        System.getProperties().forEach((k,v)->{
            System.out.println(k+"-->"+v);
        });

    }
}