package com.gateway.route;

import java.util.Map;
import java.util.Properties;

/**
 * 对路由表单例的封装，简化代码中使用
 * @author lw
 */
public class RouteTable {
    static final private RouteTableSingleton route = RouteTableSingleton.getInstance();

    static public void initTable(Properties properties) {
        route.initTable(properties);
    }

    static public Map<String, String> getTarget(String url) {
        return route.getTarget(url);
    }
}
