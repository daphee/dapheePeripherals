package com.daphee.dapheePeripherals.peripherals;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface LuaMethod {
    String name() default "[none]";
    Arg[] args() default {};
    LuaType returnType() default LuaType.VOID;
    
}
