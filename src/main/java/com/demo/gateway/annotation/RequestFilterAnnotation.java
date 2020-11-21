package com.demo.gateway.annotation;

import org.springframework.stereotype.Component;

import java.lang.annotation.*;

/**
 * 自定义注解
 * 用于标注是否需要对运行前的Request进行过滤处理
 * @author lw
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Component
@Documented
public @interface RequestFilterAnnotation {
}
