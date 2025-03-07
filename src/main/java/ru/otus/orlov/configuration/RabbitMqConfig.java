package ru.otus.orlov.configuration;


import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.rabbit.listener.adapter.MessageListenerAdapter;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.otus.orlov.services.PostService;

/**
 * Конфигурационный класс для настройки RabbitMQ.
 * Этот класс определяет бины для очередей, фабрики соединений, шаблона RabbitTemplate,
 * конвертера сообщений и контейнера для прослушивания сообщений.
 */
@Configuration
public class RabbitMqConfig {

    /**
     * Создает и возвращает очередь для обновления кэша.
     *
     * @return Очередь с именем "cacheUpdateQueue" и durable=true.
     */
    @Bean
    public Queue cacheUpdateQueue() {
        return new Queue("cacheUpdateQueue", true); // durable=true
    }

    /**
     * Создает и возвращает очередь для запросов на публикацию.
     *
     * @return Очередь с именем "postRequestQueue" и durable=false.
     */
    @Bean
    public Queue postRequestQueue() {
        return new Queue("postRequestQueue", false);
    }

    /**
     * Создает и возвращает очередь для ответов на публикацию.
     *
     * @return Очередь с именем "postResponseQueue" и durable=false.
     */
    @Bean
    public Queue postResponseQueue() {
        return new Queue("postResponseQueue", false);
    }

    /**
     * Создает и возвращает фабрику соединений для RabbitMQ.
     *
     * @return Фабрика соединений, настроенная на localhost:5672 с учетными данными "guest"/"guest".
     */
    @Bean
    public ConnectionFactory connectionFactory() {
        final CachingConnectionFactory connectionFactory = new CachingConnectionFactory();
        connectionFactory.setHost("localhost");
        connectionFactory.setPort(5672);
        connectionFactory.setUsername("guest");
        connectionFactory.setPassword("guest");
        return connectionFactory;
    }

    /**
     * Создает и возвращает шаблон RabbitTemplate для отправки сообщений.
     *
     * @param connectionFactory Фабрика соединений, используемая для создания шаблона.
     * @return RabbitTemplate с настроенным конвертером сообщений.
     */
    @Bean
    public RabbitTemplate rabbitTemplate(final ConnectionFactory connectionFactory) {
        final RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(jsonMessageConverter());
        return rabbitTemplate;
    }

    /**
     * Создает и возвращает конвертер сообщений в формате JSON.
     *
     * @return Конвертер сообщений, использующий Jackson для преобразования в JSON.
     */
    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    /**
     * Создает и возвращает контейнер для прослушивания сообщений из очереди "postRequestQueue".
     *
     * @param connectionFactory Фабрика соединений, используемая для создания контейнера.
     * @param listenerAdapter   Адаптер для обработки сообщений.
     * @return Контейнер для прослушивания сообщений, настроенный на очередь "postRequestQueue".
     */
    @Bean
    public SimpleMessageListenerContainer messageListenerContainer(final ConnectionFactory connectionFactory,
                                                                   final MessageListenerAdapter listenerAdapter) {
        final SimpleMessageListenerContainer container = new SimpleMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        container.setQueueNames("postRequestQueue");
        container.setMessageListener(listenerAdapter);
        return container;
    }

    /**
     * Создает и возвращает адаптер для обработки сообщений, используя метод "processPostRequest" из сервиса PostService.
     *
     * @param postService Сервис, который будет обрабатывать сообщения.
     * @return Адаптер для обработки сообщений.
     */
    @Bean
    public MessageListenerAdapter listenerAdapter(final PostService postService) {
        return new MessageListenerAdapter(postService, "processPostRequest");
    }
}
