package com.demo.gateway.aspect;

import com.demo.gateway.filter.Filter;
import com.sun.corba.se.impl.ior.OldJIDLObjectKeyTemplate;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.stereotype.Component;

/**
 * @author lw
 */
@Aspect
@Component
public class ResponseFilterAspect {

    @Pointcut("@annotation(com.demo.gateway.annotation.RequestFilterAnnotation)")
    public void clientExecute() {}

    @AfterReturning(value = "clientExecute()", returning = "result")
    public void requestFilter(JoinPoint point, Object result) {
        FullHttpResponse response = (FullHttpResponse) result;
        Filter.responseProcess(response);
    }
}
