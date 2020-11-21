package com.demo.gateway.jms;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jms.core.JmsMessagingTemplate;
import org.springframework.stereotype.Component;

import javax.jms.Topic;
import java.util.Map;

/**
 * mq request topic producer
 * @author lw
 */
@Component
public class RequestProducerController {

    private final JmsMessagingTemplate jmsMessagingTemplate;
    
    private final Topic topic;

    public RequestProducerController(@Qualifier("requestJmsTemplate") JmsMessagingTemplate jmsMessagingTemplate, @Qualifier("RequestTopic") Topic topic) {
        this.jmsMessagingTemplate = jmsMessagingTemplate;
        this.topic = topic;
    }

    /**
     * 发送消息
     */
    public void sendMessage(final Map<String, String> message){
        jmsMessagingTemplate.convertAndSend(topic, message);
    }
}
