package com.demo.gateway.aspect;

import com.demo.gateway.route.RouteTable;
import io.netty.handler.codec.http.FullHttpRequest;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

/**
 * @author lw
 */
@Aspect
@Component
public class RouteAspect {

    @Pointcut("@annotation(com.demo.gateway.annotation.RouteAnnotation)")
    public void clientExecute() {}

    @Before("clientExecute()")
    public void convert(JoinPoint point) {
        Object[] args = point.getArgs();
        FullHttpRequest request = (FullHttpRequest) args[0];
        request.setUri(RouteTable.getTargetUrl(request.uri()));
    }
}
