package com.gateway.client;


public class ClientAspect {

//    @Before(value = "execution(* com.gateway.client.Client.*(..)) and args(request)")
//    public void beforeAdvice(JoinPoint joinPoint, FullHttpRequest request) {
//        System.out.println("Before method:" + joinPoint.getSignature());
//        // 请求过滤处理
//        Filter.requestProcess(request);
//        System.out.println("filter request::" + request.toString());
//    }

//    @Pointcut(value="execution(* com.gateway.client.CustomClientAsync.*(..))")
//    public void point(){
//
//    }
//
//    @Before(value="point()")
//    public void before(){
//        System.out.println("========>begin klass dong...");
//    }
//
//    @AfterReturning(value = "point()")
//    public void after(){
//        System.out.println("========>after klass dong...");
//    }
//
//    @Around("point()")
//    public void around(ProceedingJoinPoint joinPoint) throws Throwable{
//        System.out.println("========>around begin klass dong");
//        joinPoint.proceed();
//        System.out.println("========>around after klass dong");
//
//    }
}
