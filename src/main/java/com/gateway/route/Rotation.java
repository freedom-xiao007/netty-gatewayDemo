package com.gateway.route;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * 负载均衡：轮询算法
 * @author lw
 */
public class Rotation implements LoadBalance {

    public ConcurrentMap<String, Integer> serverFlag;
    public Map<String, Integer> serverAmount;

    /**
     * 得到相关服务器集群的数量并初始化起始标记位置
     * @param server
     */
    public Rotation(Map<String, List<String>> server) {
        serverFlag = new ConcurrentHashMap<>(server.size());
        serverAmount = new HashMap<>(server.size());
        for (String serverGroup: server.keySet()) {
            serverFlag.put(serverGroup, 0);
            serverAmount.put(serverGroup, server.get(serverGroup).size());
        }
    }

    /**
     * 返回当前标记位的服务器地址，标记位向后移动一位
     * @param server 所有服务器列表
     * @param serverGroup 服务器组名称
     * @return
     */
    @Override
    public String get(Map<String, List<String>> server, String serverGroup) {
        int index = serverFlag.get(serverGroup);
        String target = server.get(serverGroup).get(index);
        int nextIndex = serverFlag.get(serverGroup) + 1;
        if (nextIndex >= serverAmount.get(serverGroup)) {
            nextIndex = 0;
        }
        serverFlag.put(serverGroup, nextIndex);
//        System.out.println("balance::" + target + "::" + index + "   next::" + serverFlag.get(serverGroup));
        return target;
    }
}
