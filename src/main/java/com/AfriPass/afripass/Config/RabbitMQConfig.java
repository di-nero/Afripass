package com.AfriPass.afripass.Config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
@Configuration
public class RabbitMQConfig {

    public static final String EXCHANGE = "ticketing.exchange";
    public static final String QUEUE = "notification.queue";
    public static final String ROUTING_KEY = "ticket.confirmed";
    public static final String DEAD_LETTER_QUEUE = "dead.letter.queue";

    @Bean
    public DirectExchange exchange(){
        return new DirectExchange(EXCHANGE);
    }

    @Bean
    public Queue notificationQueue(){

        return QueueBuilder
                .durable(QUEUE)
                .withArgument("x-dead-letter-exchange", "")
                .withArgument("x-dead-letter-routing-key" , DEAD_LETTER_QUEUE)
                .build();
    }

    @Bean
    public Queue deadLetterQueue(){
        return QueueBuilder
                .durable(DEAD_LETTER_QUEUE)
                .build();
    }

    @Bean
    public Binding binding(Queue notificationQueue , DirectExchange exchange){
        return BindingBuilder.bind(notificationQueue).to(exchange).with(ROUTING_KEY);
    }


    @Bean
    public MessageConverter jsonMessageConverter() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        return new Jackson2JsonMessageConverter(objectMapper);
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter());
        return template;
    }

}

