package com.yinbing.jvm;

public class User {
    static {
        System.out.println("自定义类加载器");
    }

    public void print(){
        System.out.println("自定义类加载器 : print");
    }
}
