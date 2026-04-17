package com.lonelyash.common.config;

import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MqConfig {

    @Bean
    //用于自定义 RabbitMQ 的消息序列化方式。通过配置 Jackson2JsonMessageConverter 替换默认的 JDK 序列化，
    // 实现消息的 JSON 格式传输，解决默认序列化方式（如体积大、可读性差、安全风险高）的问题
    public MessageConverter messageConverter() {
        Jackson2JsonMessageConverter converter = new Jackson2JsonMessageConverter();
        converter.setCreateMessageIds(true);//为每条消息自动生成唯一 ID
        return converter;
    }
}
