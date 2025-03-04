package ru.otus.orlov.repositories;

import java.util.Set;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.otus.orlov.entity.Post;

/** Репозиторий для работы с постами */
@Repository
public interface PostRepository extends JpaRepository<Post, Long> {
    /** Получить посты друзей по ид */
    @Query("SELECT post FROM Post post WHERE post.id IN " +
            "(SELECT posts.id FROM User user JOIN user.posts posts WHERE user.id IN :friendIds) " +
            "ORDER BY post.createdAt DESC")
    PageImpl<Post> findPostsByFriendIds(@Param("friendIds") final Set<Long> friendIds, final Pageable pageable);
}
