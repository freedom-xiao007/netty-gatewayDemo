package com.gateway.route;

import com.google.common.base.Joiner;

import java.util.*;

/**
 * @author lw
 */
public class RouteTableSingleton {

    private enum EnumSingleton {
        /**
         * 懒汉枚举单例
         */
        INSTANCE;
        private RouteTableSingleton instance = null;

        private EnumSingleton(){
            instance = new RouteTableSingleton();
        }
        public RouteTableSingleton getSingleton(){
            return instance;
        }
    }

    public static RouteTableSingleton getInstance(){
        return EnumSingleton.INSTANCE.getSingleton();
    }

    private Map<String, Map<String, String>> table = new HashMap<>(16);

    /**
     * 从配置文件中读取数据初始化路由表
     * @param properties
     */
    public void initTable(Properties properties) {
        String hosts = (String) properties.get("route.rule.hots");
        List<String> sources = Arrays.asList(hosts.split(" "));

        String prefix = "route.rule.hosts.";
        String ip = "ip";
        String port = "port";
        for (String source: sources) {
            Map<String, String> target = new HashMap<>(2);
            target.put("address", (String) properties.get(prefix + source + "." + ip));
            target.put("port", (String) properties.get(prefix + source + "." + port));
            table.put(source, target);
        }

        System.out.println("Load route table end::");
        for (String source: table.keySet()) {
            System.out.println(source + " --> " + table.get(source));
        }
    }

    /**
     * 根据用户输入的源地址，得到路由表中对应的服务地址
     * @param url 用户输入的源URL
     * @return
     */
    public Map<String, String> getTarget(String url) {
        List<String> path = Arrays.asList(url.split("/"));
//        System.out.println("path::" + path.toString());
        String source = path.get(1);
        if (!table.containsKey(source)) {
            return null;
        }

        Map<String, String> target = new HashMap<>(3);
        target.putAll(table.get(source));
        target.put("url", "/" + Joiner.on("/").join(path.subList(2, path.size())));
        return target;
    }
}
