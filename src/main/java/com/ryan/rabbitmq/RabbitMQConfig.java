package com.ryan.rabbitmq;

import com.rabbitmq.client.Channel;
import com.ryan.rabbitmq.converter.ImageMessageConverter;
import com.ryan.rabbitmq.converter.PDFMessageConverter;
import com.ryan.rabbitmq.converter.TextMessageConverter;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.ChannelAwareMessageListener;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.rabbit.listener.adapter.MessageListenerAdapter;
import org.springframework.amqp.support.ConsumerTagStrategy;
import org.springframework.amqp.support.converter.ContentTypeDelegatingMessageConverter;
import org.springframework.amqp.support.converter.DefaultJackson2JavaTypeMapper;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * @author ryan
 * @version Id: RabbitMQConfig, v 0.1 2022/10/12 3:52 PM ryan Exp $
 */
@Configuration
@ComponentScan({"com.ryan.rabbitmq.*"})
public class RabbitMQConfig {

    @Bean
    public ConnectionFactory connectionFactory() {
        CachingConnectionFactory connectionFactory = new CachingConnectionFactory();
        connectionFactory.setAddresses("20.50.54.194:5672");
        connectionFactory.setUsername("guest");
        connectionFactory.setPassword("guest");
        connectionFactory.setVirtualHost("my_rabbit");
        return connectionFactory;
    }

    @Bean
    public RabbitAdmin rabbitAdmin(ConnectionFactory connectionFactory) {
        RabbitAdmin rabbitAdmin = new RabbitAdmin(connectionFactory);
        rabbitAdmin.setAutoStartup(true);
        return rabbitAdmin;
    }

    /**
     * ?????????????????????
     * 1. ?????????????????????
     * 2. ???????????????????????????
     * ??????????????????????????????????????????????????????????????????key?????????
     * FanoutExchange: ?????????????????????????????????????????????routingkey?????????
     * HeadersExchange ?????????????????????key-value??????
     * DirectExchange: ??????routing key?????????????????????
     * TopicExchange:??????????????????
     *
     * @return
     */
    @Bean
    public TopicExchange exchange001() {
        return new TopicExchange("topic001", true, false);
    }

    @Bean
    public Queue queue001() {
        return new Queue("queue001", true);
    }

    @Bean
    public Queue queue002() {
        return new Queue("queue002", true);
    }

    @Bean
    public Binding binding001() {
        return BindingBuilder.bind(queue001()).to(exchange001()).with("spring.#");
    }

    @Bean
    public Binding binding002() {
        return BindingBuilder.bind(queue002()).to(exchange001()).with("mq.#");
    }

    @Bean
    public Queue queue_image() {
        return new Queue("image_queue", true);
    }

    @Bean
    public Queue queue_pdf() {
        return new Queue("pdf_queue", true);
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        return rabbitTemplate;
    }

    @Bean
    public SimpleMessageListenerContainer messageListenerContainer(ConnectionFactory connectionFactory) {
        SimpleMessageListenerContainer container = new SimpleMessageListenerContainer(connectionFactory);
        // ??????????????????
        container.setQueues(queue001(), queue002(), queue_image(), queue_pdf());
        // ??????????????????????????????
        container.setConcurrentConsumers(1);
        // ???????????????????????????
        container.setMaxConcurrentConsumers(5);
        // ????????????,?????????????????????
        container.setDefaultRequeueRejected(false);
        // ack????????????,auto:????????????
        container.setAcknowledgeMode(AcknowledgeMode.AUTO);
        // ????????????????????????
        container.setConsumerTagStrategy(new ConsumerTagStrategy() {
            @Override
            public String createConsumerTag(String queue) {
                return queue + "_" + UUID.randomUUID().toString();
            }
        });
        // 1. ??????
//        container.setMessageListener(new ChannelAwareMessageListener() {
//            @Override
//            public void onMessage(Message message, Channel channel) throws Exception {
//                String msg = new String(message.getBody());
//                System.out.println("------ ????????? ???" + msg);
//            }
//        });

//        // 2. ?????????
//        MessageListenerAdapter adapter = new MessageListenerAdapter(new MessageDelegate());
//        adapter.setDefaultListenerMethod("consumeMessage");
//        // ?????????????????????
//        adapter.setMessageConverter(new TextMessageConverter());
//        container.setMessageListener(adapter);
//

//         // 3. ?????????????????????????????????
//        MessageListenerAdapter adapter = new MessageListenerAdapter(new MessageDelegate());
//        adapter.setMessageConverter(new TextMessageConverter());
//        Map<String, String> queueOrTagToMethodName = new HashMap<>();
//        queueOrTagToMethodName.put("queue001", "method1");
//        queueOrTagToMethodName.put("queue002", "method2");
//        adapter.setQueueOrTagToMethodName(queueOrTagToMethodName);
//
//        container.setMessageListener(adapter);

//        // 1.1 ??????json??????????????????
//        MessageListenerAdapter adapter = new MessageListenerAdapter(new MessageDelegate());
//        adapter.setDefaultListenerMethod("consumeMessage");
//
//        Jackson2JsonMessageConverter jackson2JsonMessageConverter = new Jackson2JsonMessageConverter();
//        adapter.setMessageConverter(jackson2JsonMessageConverter);
//
//        container.setMessageListener(adapter);


//        // 1.2 DefaultJackson2JavaTypeMapper & Jackson2JsonMessageConverter ??????java????????????
//         MessageListenerAdapter adapter = new MessageListenerAdapter(new MessageDelegate());
//         adapter.setDefaultListenerMethod("consumeMessage");
//
//         Jackson2JsonMessageConverter jackson2JsonMessageConverter = new Jackson2JsonMessageConverter();
//
//         // ??????java?????????????????????
//         DefaultJackson2JavaTypeMapper javaTypeMapper = new DefaultJackson2JavaTypeMapper();
//         jackson2JsonMessageConverter.setJavaTypeMapper(javaTypeMapper);
//
//         adapter.setMessageConverter(jackson2JsonMessageConverter);
//         container.setMessageListener(adapter);


        //1.3 DefaultJackson2JavaTypeMapper & Jackson2JsonMessageConverter ??????java?????????????????????
//         MessageListenerAdapter adapter = new MessageListenerAdapter(new MessageDelegate());
//         adapter.setDefaultListenerMethod("consumeMessage");
//         Jackson2JsonMessageConverter jackson2JsonMessageConverter = new Jackson2JsonMessageConverter();
//         DefaultJackson2JavaTypeMapper javaTypeMapper = new DefaultJackson2JavaTypeMapper();
//
//         Map<String, Class<?>> idClassMapping = new HashMap<String, Class<?>>();
//         idClassMapping.put("order", com.ryan.rabbitmq.entity.Order.class);
//         idClassMapping.put("packaged", com.ryan.rabbitmq.entity.Packaged.class);
//         javaTypeMapper.setIdClassMapping(idClassMapping);
//
//         jackson2JsonMessageConverter.setJavaTypeMapper(javaTypeMapper);
//         adapter.setMessageConverter(jackson2JsonMessageConverter);
//         container.setMessageListener(adapter);

        //1.4 ext convert
        MessageListenerAdapter adapter = new MessageListenerAdapter(new MessageDelegate());
        adapter.setDefaultListenerMethod("consumeMessage");

        //??????????????????:
        ContentTypeDelegatingMessageConverter convert = new ContentTypeDelegatingMessageConverter();

        TextMessageConverter textConvert = new TextMessageConverter();
        convert.addDelegate("text", textConvert);
        convert.addDelegate("html/text", textConvert);
        convert.addDelegate("xml/text", textConvert);
        convert.addDelegate("text/plain", textConvert);

        Jackson2JsonMessageConverter jsonConvert = new Jackson2JsonMessageConverter();
        convert.addDelegate("json", jsonConvert);
        convert.addDelegate("application/json", jsonConvert);

        ImageMessageConverter imageConverter = new ImageMessageConverter();
        convert.addDelegate("image/png", imageConverter);
        convert.addDelegate("image", imageConverter);

        PDFMessageConverter pdfConverter = new PDFMessageConverter();
        convert.addDelegate("application/pdf", pdfConverter);

        adapter.setMessageConverter(convert);
        container.setMessageListener(adapter);


        return container;
    }


}
