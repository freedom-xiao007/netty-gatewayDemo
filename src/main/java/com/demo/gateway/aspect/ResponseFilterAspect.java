package com.demo.gateway.aspect;

import com.demo.gateway.filter.Filter;
import io.netty.handler.codec.http.FullHttpResponse;
import org.aspectj.lang.annotation.*;
import org.springframework.stereotype.Component;

/**
 * Response返回过滤处理切面
 * @author lw
 */
@Aspect
@Component
public class ResponseFilterAspect {

    @Pointcut("@annotation(com.demo.gateway.annotation.RequestFilterAnnotation)")
    public void clientExecute() {}

    @AfterReturning(value = "clientExecute()", returning = "result")
    public void requestFilter(Object result) {
        FullHttpResponse response = (FullHttpResponse) result;
        Filter.responseProcess(response);
    }
}
