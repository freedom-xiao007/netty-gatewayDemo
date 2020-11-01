package com.gateway.route;

import java.util.Map;
import java.util.Properties;

/**
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
