package ru.otus.orlov.services;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.io.IOException;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.otus.orlov.dto.PageImplDto;
import ru.otus.orlov.entity.Post;
import ru.otus.orlov.repositories.PostRepository;
import ru.otus.orlov.repositories.UserRepository;

/** Сервис работы с постами */
@Slf4j
@RequiredArgsConstructor
@Service
public class PostServiceImpl implements PostService {

    /** Репозиторий для работы с постами */
    private final PostRepository postRepository;

    /** Репозиторий для работы с данными пользователя */
    private final UserRepository userRepository;

    /** Template Очереди Rabbit */
    private final RabbitTemplate rabbitTemplate;


    @Transactional(readOnly = true)
    @Cacheable(value = "feed", key = "#userId")
    public PageImplDto<Post> getFeed(final Long userId, final int offset, final int limit) {
        log.info("Идем в БД за данными по пользователю с id {}", userId);
        // Отправляем запрос в очередь (передаем userId, offset и limit как сообщение)
        rabbitTemplate.convertAndSend("postRequestQueue", new Object[]{userId, offset, limit});
        // Ждем ответа из очереди
        final byte[] responseBytes = (byte[]) rabbitTemplate
                .receiveAndConvert("postResponseQueue", 5000);
        if (responseBytes == null) {
            throw new IllegalStateException("Не удалось извлечь записи из очереди");
        }
        // Десериализуем ответ в DTO
        final ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule()); // Регистрируем модуль для поддержки Java 8 date/time
        try {
            return objectMapper.readValue(responseBytes, new TypeReference<PageImplDto<Post>>() {
            });
        } catch (final IOException e) {
            log.error("Ошибка при десериализации сообщения", e);
            throw new RuntimeException("Ошибка при обработке сообщения", e);
        }
    }

    @RabbitListener(queues = "postRequestQueue")
    public void processPostRequest(final byte[] messageBytes) {
        log.info("Обработка запроса на получение постов");
        final ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule()); // Регистрируем модуль для поддержки Java 8 date/time
        try {
            final Object[] requestData = objectMapper.readValue(messageBytes, Object[].class);
            final Long userId = ((Number) requestData[0]).longValue();
            final int offset = ((Number) requestData[1]).intValue();
            final int limit = ((Number) requestData[2]).intValue();
            final Set<Long> friendIds = userRepository.findFriendIdsByUserId(userId);
            final PageImpl<Post> posts = postRepository
                    .findPostsByFriendIds(friendIds, PageRequest.of(offset / limit, limit));
            final PageImplDto<Post> dto = new PageImplDto<>();
            dto.setContent(posts.getContent());
            dto.setPageNumber(posts.getPageable().getPageNumber());
            dto.setPageSize(posts.getPageable().getPageSize());
            dto.setTotalElements(posts.getTotalElements());
            rabbitTemplate.convertAndSend("postResponseQueue", objectMapper.writeValueAsBytes(dto));
        } catch (final IOException e) {
            log.error("Ошибка при десериализации сообщения", e);
            throw new RuntimeException("Ошибка при обработке сообщения", e);
        }
    }
}
