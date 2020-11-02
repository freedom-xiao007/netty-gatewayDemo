package com.gateway.route;

import com.gateway.util.Config;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.Map;

public class RouteTableSingletonTest {

    @Before
    public void setUp() throws IOException {
        Config.init("test.properties");
    }

    @Test
    public void getTargetTest() {
        String url = "/host1/get/url";
        Map<String, String> target = RouteTableSingleton.getInstance().getTarget(url);
        System.out.println(target.toString());
        assert target.get("address").equals("localhost");
        assert target.get("port").equals("8080");
        assert target.get("url").equals("/get/url");

        url = "/host3/get/url";
        assert RouteTableSingleton.getInstance().getTarget(url) == null;
    }

    /**
     * 检测路由转发是否正常，轮询是否生效
     */
    @Test
    public void readJsonConfigTest() {
        System.out.println("/get/url/hello".indexOf("/get"));

        String url = RouteTable.getTargetUrl("/greeting");
        assert url.equals( "http://192.168.101.105:8080");

        url = RouteTable.getTargetUrl("/hello");
        assert url.equals( "http://192.168.101.105:8082");

        url = RouteTable.getTargetUrl("/greeting");
        assert url.equals( "http://192.168.101.105:8081");

        url = RouteTable.getTargetUrl("/hello");
        assert url.equals( "http://192.168.101.105:8083");

        url = RouteTable.getTargetUrl("/greeting");
        assert url.equals( "http://192.168.101.105:8080");
    }
}
