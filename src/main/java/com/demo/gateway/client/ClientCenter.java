package com.demo.gateway.client;

import com.demo.gateway.common.CreatResponse;
import com.demo.gateway.filter.Filter;
import com.demo.gateway.route.RouteTable;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * 使用时请进行初始化操作
 * 客户端中心
 * 起一个中介中用，获取后台服务器结果，调用server outbound返回结果
 * @author lw
 */
@Deprecated
public class ClientCenter {

    private static final Logger logger = LoggerFactory.getLogger(ClientCenter.class);

    private static final CustomClientSync client = new CustomClientSync();

    /**
     * 将请求转发到后台服务器，获得响应后返回给用户
     * @param request 请求
     * @param serverOutbound server outbound
     */
    static public void execute(FullHttpRequest request, Channel serverOutbound) {
        // 路由转发处理,负载均衡
        String source = request.uri();
        String target = RouteTable.getTargetUrl(source);
        if (target == null) {
            logger.error("url: " + source + " can't find in route");
            try {
                serverOutbound.writeAndFlush(CreatResponse.creat404(request)).sync();
            } catch (InterruptedException e) {
                e.printStackTrace();
                logger.error("return 404 failed");
            }
            return;
        }

        request.setUri(target);

        // 请求过滤处理
        Filter.requestProcess(request);

        FullHttpResponse response = client.execute(request, serverOutbound);
        if (response == null) {
            logger.error("backend server return null");
        }

        // 相应过滤处理
        Filter.responseProcess(response);

        // 返回Response数据给用户
        try {
            serverOutbound.writeAndFlush(response).sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
