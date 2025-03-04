package ru.otus.orlov.controller;

import java.util.HashSet;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.otus.orlov.dto.PageImplDto;
import ru.otus.orlov.entity.Post;
import ru.otus.orlov.services.PostService;

@RestController
@RequiredArgsConstructor
public class PostController {
    /** Сервис работы с постами */
    private final PostService postService;


    @GetMapping("/api/v1/post/feed/{id}")
    public Set<Post> getFeed(
            @PathVariable("id") final Long id,
            @RequestParam(defaultValue = "0") final int page,
            @RequestParam(defaultValue = "50") final int size
    ) {
        final PageImplDto<Post> feed = postService.getFeed(id, page, size);
        return new HashSet<>(feed.getContent());
    }
}
