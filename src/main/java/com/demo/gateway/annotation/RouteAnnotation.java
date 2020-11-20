package com.demo.gateway.annotation;

import org.springframework.stereotype.Component;

import java.lang.annotation.*;

/**
 * @author lw
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Component
@Documented
public @interface RouteAnnotation {
}
