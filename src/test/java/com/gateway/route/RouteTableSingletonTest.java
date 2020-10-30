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
}
