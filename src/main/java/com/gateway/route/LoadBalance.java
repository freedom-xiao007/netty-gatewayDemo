package com.gateway.route;

import java.util.List;
import java.util.Map;

/**
 * 负载均衡算法接口
 * @author lw
 */
public interface LoadBalance {

    /**
     * 返回当前服务器组中下一个应该接受请求的机器地址
     * @param server 所有服务器列表
     * @param serverGroup 服务器组名称
     * @return 机器地址
     */
    String get(Map<String, List<String>> server, String serverGroup);
}
