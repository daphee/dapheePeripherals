package com.daphee.dapheePeripherals.peripherals;

import java.lang.reflect.Method;

public class MethodDeclaration {
    private LuaMethod annotation;
    private Method method;
    private FunctionAdapter obj;
    public MethodDeclaration(LuaMethod annotation, Method method, FunctionAdapter obj){
        this.annotation = annotation;
        this.method = method;
        this.obj = obj;
    }
    
    public String getName(){
        if( this.annotation.name().equals("[none]"))
           return method.getName();
        return this.annotation.name();
    }
    
    public LuaMethod getAnnotation(){
        return this.annotation;
    }
    
    public Method getMethod(){
        return this.method;
    }
    
    public FunctionAdapter getAdapter(){
        return this.obj;
    }
}
