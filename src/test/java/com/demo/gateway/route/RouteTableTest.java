package com.demo.gateway.route;

import org.junit.jupiter.api.Test;

public class RouteTableTest {

    /**
     * 检测转换是否正确
     */
    @Test
    public void getTargetUrlTest() {
        RouteTable.initTable();
        String url = "/group1/";
        String target = RouteTable.getTargetUrl(url);
        System.out.println(target);
        assert "http://192.168.101.104:8080/".equals(target);
    }

    /**
     * 检测路由转发是否正常，轮询是否生效
     */
    @Test
    public void readJsonConfigTest() {
        String url = RouteTable.getTargetUrl("/group2/");
        System.out.println(url);
        assert url.equals( "http://192.168.101.104:8080/");

        url = RouteTable.getTargetUrl("/group2/");
        System.out.println(url);
        assert url.equals( "http://192.168.101.104:8081/");
    }

}
