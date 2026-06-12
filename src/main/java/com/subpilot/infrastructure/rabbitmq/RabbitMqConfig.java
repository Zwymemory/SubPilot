package com.subpilot.infrastructure.rabbitmq;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMqConfig {

    @Bean
    public DirectExchange reminderExchange() {
        return new DirectExchange(RabbitMqConstants.REMINDER_EXCHANGE, true, false);
    }

    @Bean
    public Queue reminderQueue() {
        return new Queue(RabbitMqConstants.REMINDER_QUEUE, true);
    }

    @Bean
    public Binding billingReminderBinding(Queue reminderQueue, DirectExchange reminderExchange) {
        return BindingBuilder.bind(reminderQueue)
                .to(reminderExchange)
                .with(RabbitMqConstants.ROUTING_KEY_BILLING);
    }

    @Bean
    public Binding expiringReminderBinding(Queue reminderQueue, DirectExchange reminderExchange) {
        return BindingBuilder.bind(reminderQueue)
                .to(reminderExchange)
                .with(RabbitMqConstants.ROUTING_KEY_EXPIRING);
    }

    @Bean
    public Binding overdueReminderBinding(Queue reminderQueue, DirectExchange reminderExchange) {
        return BindingBuilder.bind(reminderQueue)
                .to(reminderExchange)
                .with(RabbitMqConstants.ROUTING_KEY_OVERDUE);
    }

    @Bean
    public Jackson2JsonMessageConverter jackson2JsonMessageConverter(ObjectMapper objectMapper) {
        return new Jackson2JsonMessageConverter(objectMapper);
    }

    @Bean
    public RabbitTemplate rabbitTemplate(
            ConnectionFactory connectionFactory,
            Jackson2JsonMessageConverter jackson2JsonMessageConverter
    ) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(jackson2JsonMessageConverter);
        return rabbitTemplate;
    }

    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            ConnectionFactory connectionFactory,
            Jackson2JsonMessageConverter jackson2JsonMessageConverter
    ) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(jackson2JsonMessageConverter);
        factory.setDefaultRequeueRejected(false);
        return factory;
    }
}
