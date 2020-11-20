package com.demo.gateway.filter;

import com.demo.gateway.client.CustomClientAsync;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * 过滤器
 * @author lw
 */
@Component
@Order(1)
public class Filter implements ApplicationRunner {

    private static final Logger logger = LoggerFactory.getLogger(CustomClientAsync.class);

    static final FilterSingleton filterSingleton = FilterSingleton.getInstance();

    static private void addRequestFilter(RequestFilter requestFrontFilter) {
        filterSingleton.registerRequestFrontFilter(requestFrontFilter);
    }

    static private void addResponseFilter(ResponseFilter responseBackendFilter) {
        filterSingleton.registerResponseBackendFilter(responseBackendFilter);
    }

    /**
     * 在这个方法中添加Request的过滤操作类,在启动函数中进行调用
     */
    static public void initRequestFilter() {
        addRequestFilter(new MethodToPost());
    }

    /**
     * 在这个方法中添加Response的过滤操作类，在启动函数中进行调用
     */
    static public void initResponseFilter() {
        addResponseFilter(new AddGatewayInfo());
    }

    /**
     * 遍历Request过滤操作链，对Request进行处理，在Server inbound接收到Request后进行调用
     * @param request
     */
    static public void requestProcess(HttpRequest request) {
        for (RequestFilter filter: filterSingleton.getRequestFrontFilterList()) {
            filter.filter(request);
        }
    }

    /**
     * 调用Response过滤操作链，对Response进行处理，在Server outbound发送Response前进行调用
     * @param response
     */
    static public void responseProcess(HttpResponse response) {
        for (ResponseFilter filter: filterSingleton.getResponseBackendFilters()) {
            filter.filter(response);
        }
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        Filter.initRequestFilter();
        Filter.initResponseFilter();
        logger.info("Filter line load successfully");
    }
}
