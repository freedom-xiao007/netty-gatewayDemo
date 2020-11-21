package com.demo.gateway.jms;

import com.demo.gateway.client.ClientAsync;
import com.demo.gateway.common.CreateRequest;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import java.util.Map;

import static com.demo.gateway.common.Constant.*;

/**
 * mq request topic consumer
 * @author lw
 */
@Component
public class RequestTopicConsumerListener {

    private final ClientAsync clientAsync;

    public RequestTopicConsumerListener(ClientAsync clientAsync) {
        this.clientAsync = clientAsync;
    }

    /**
     * topic模式的消费者
     */
    @JmsListener(destination = "${spring.activemq.request-topic-name}", containerFactory = "RequestTopicListener")
    public void readActiveQueue(Map<String, String> message) {
        String method = message.get(REQUEST_METHOD);
        String uri = message.get(REQUEST_URI);
        String version = message.get(REQUEST_VERSION);
        clientAsync.sendRequest(CreateRequest.create(method, uri, version), Integer.parseInt(message.get(CHANNEL_HASH_CODE)));
    }
}