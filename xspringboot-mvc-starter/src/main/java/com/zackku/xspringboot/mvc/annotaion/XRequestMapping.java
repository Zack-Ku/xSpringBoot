package com.zackku.xspringboot.mvc.annotaion;

import io.vertx.core.http.HttpMethod;
import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author Zack
 * @date 2020/6/3
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface XRequestMapping {
    @AliasFor("path")
    String[] value() default {};

    @AliasFor("value")
    String[] path() default {};

    HttpMethod[] method() default {};
}
