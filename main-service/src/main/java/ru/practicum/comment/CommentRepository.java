package ru.practicum.comment;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findAllByAuthorId(Long userId, Pageable pageable);

    List<Comment> findAllByEventId(Long eventId, Pageable pageable);

    void deleteByIdAndAuthorId(Long commentId, Long userId);

    @Query(" SELECT i " +
            "FROM Comment i " +
            "WHERE UPPER(i.text) LIKE UPPER(CONCAT('%', ?1, '%'))")
    List<Comment> search(String text, Pageable pageable);
}
