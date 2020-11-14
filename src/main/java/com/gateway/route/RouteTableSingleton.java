package com.gateway.route;

import com.google.common.base.Joiner;
import com.google.gson.Gson;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
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
        private RouteTableSingleton instance;

        EnumSingleton(){
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
    private Map<String, List<String>> server;
    private List<Map<String, String>> route;
    /**
     * 负载均衡：轮询算法
     */
    private Rotation rotationBalance;

    /**
     * 从配置文件中读取数据初始化路由表
     * @param properties 配置文件
     */
    @Deprecated
    public void initTable(Properties properties) {
        String hosts = (String) properties.get("route.rule.hots");
        String[] sources = hosts.split(" ");

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
     * @return 后台服务相关信息
     */
    @Deprecated
    public Map<String, String> getTarget(String url) {
        List<String> path = Arrays.asList(url.split("/"));
        String source = path.get(1);
        if (!table.containsKey(source)) {
            return null;
        }

        Map<String, String> target = new HashMap<>(3);
        target.putAll(table.get(source));
        target.put("url", "/" + Joiner.on("/").join(path.subList(2, path.size())));
        return target;
    }

    /**
     * 根据源URL，获取路由中的 目标服务器地址，内置负载均衡
     * @param url 源请求地址
     * @return 后台服务器地址
     */
    String getTargetUrl(String url) {
        for (Map<String, String> table: route) {
            String source = table.get("source");
            int index = url.indexOf(source);
            if (index == 0) {
                // 获取负载均衡后的服务器目标地址
                String target = rotationBalance.get(server, table.get("target"));
                return target + url.substring(source.length());
            }
        }
        return null;
    }

    /**
     * 读取JSON配置文件，初始化路由和负载均衡设置
     */
    void readJsonConfig() {
        String fileName = "F:\\Code\\Java\\GateWayDemo\\src\\main\\resources\\route.json";
//        String fileName = "/root/code/java/GateWayDemo/src/main/resources/route.json";
        Gson gson = new Gson();
        try (Reader reader = new FileReader(fileName)) {
            Map<String, Object> config = gson.fromJson(reader, Map.class);
            System.out.println(config.toString());

            server = (Map<String, List<String>>) config.get("server");
            System.out.println(server);

            route = (List<Map<String, String>>) config.get("route");
            System.out.println(route);

            // 初始化负载均衡
            rotationBalance = new Rotation(server);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
