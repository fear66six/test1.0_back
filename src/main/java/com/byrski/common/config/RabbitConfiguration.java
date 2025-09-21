package com.byrski.common.config;

import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static com.byrski.common.utils.Const.*;

/**
 * RabbitMQ消息队列配置
 */
@Configuration
public class RabbitConfiguration {


    @Bean
    public Queue characterImageTtlQueue() {
        return QueueBuilder.durable(QUEUE_TRADE_TTL)
                .withArgument("x-dead-letter-exchange", "")
                .withArgument("x-dead-letter-routing-key", QUEUE_TRADE_DEAD)
                .withArgument("x-message-ttl", 1200000)
                .build();
    }

    @Bean
    public Queue characterImageDeadQueue() {
        return QueueBuilder.durable(QUEUE_TRADE_DEAD)
                .build();
    }

    @Bean("emailQueue")
    public Queue emailQueue() {
        return QueueBuilder
                .durable(QUEUE_EMAIL)
                .build();
    }
}
