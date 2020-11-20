package com.demo.gateway.aspect;

import com.demo.gateway.filter.Filter;
import com.demo.gateway.route.RouteTable;
import io.netty.handler.codec.http.FullHttpRequest;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

import java.lang.annotation.*;

/**
 * @author lw
 */
@Aspect
@Component
public class RequestFilterAspect {

    @Pointcut("@annotation(com.demo.gateway.annotation.RequestFilterAnnotation)")
    public void clientExecute() {}

    @Before("clientExecute()")
    public void requestFilter(JoinPoint point) {
        Object[] args = point.getArgs();
        FullHttpRequest request = (FullHttpRequest) args[0];
        Filter.requestProcess(request);
    }
}
