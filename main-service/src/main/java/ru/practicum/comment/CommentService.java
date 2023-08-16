package ru.practicum.comment;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import ru.practicum.comment.dto.CommentMapper;
import ru.practicum.comment.dto.NewCommentDto;
import ru.practicum.event.Event;
import ru.practicum.event.EventRepository;
import ru.practicum.event.util.State;
import ru.practicum.exception.ForbiddenException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.user.User;
import ru.practicum.user.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Transactional
public class CommentService {
    final CommentRepository commentRepository;
    final UserRepository userRepository;
    final EventRepository eventRepository;

    @Transactional
    public Comment saveComment(Long userId, Long eventId, NewCommentDto newCommentDto) {
        User user = userRepository.findById(userId).orElseThrow(
                () -> new NotFoundException("Пользователь не найден."));
        Event event = eventRepository.findById(eventId).orElseThrow(
                () -> new NotFoundException("Событие не найдено."));

        if(event.getState() != State.PUBLISHED) {
            throw new ForbiddenException("Оставить коммент можно только к опубликованному событию.");
        }

        Comment comment = CommentMapper.toModel(newCommentDto);
        comment.setAuthor(user);
        comment.setEvent(event);
        comment.setCreatedDate(LocalDateTime.now());
        comment.setText(newCommentDto.getText());
        return commentRepository.save(comment);
    }

    @Transactional
    public Comment updateComment(Long userId, Long commentId, NewCommentDto newCommentDto) {
        isUserExists(userId);
        Comment comment = commentRepository.findById(commentId).orElseThrow(
                () -> new NotFoundException("Коммент не найден."));

        if (!comment.getAuthor().getId().equals(userId)) {
            throw new ForbiddenException("Обновить коммент может только владелец.");
        }

        if (LocalDateTime.now().isAfter(comment.getCreatedDate().plusHours(1L))) {
            throw new ForbiddenException("Редактировать коммент можно только в течение 1 часа после создания.");
        }

        comment.setText(newCommentDto.getText());
        return comment;
    }

    @Transactional(readOnly = true)
    public List<Comment> getAllCommentsFromEvent(Long eventId, Integer from, Integer size) {
        if (!eventRepository.existsById(eventId)) {
            throw new NotFoundException("Событие не найдено.");
        }
        Pageable pageable = PageRequest.of(from / size, size);

        return commentRepository.findAllByEventId(eventId, pageable);
    }

    @Transactional(readOnly = true)
    public List<Comment> getAllCommentsFromUser(Long userId, Integer from, Integer size) {
        isUserExists(userId);
        Pageable pageable = PageRequest.of(from / size, size);

        return commentRepository.findAllByAuthorId(userId, pageable);
    }

    @Transactional(readOnly = true)
    public List<Comment> searchCommentByText(String text, Integer from, Integer size) {
        if (!StringUtils.hasText(text)) {
            return List.of();
        }

        Pageable pageable = PageRequest.of(from / size, size);

        return commentRepository.search(text, pageable);
    }

    @Transactional
    public void deleteCommentFromOwner(Long userId, Long commentId) {
        isUserExists(userId);
        commentRepository.deleteByIdAndAuthorId(commentId, userId);
    }

    @Transactional
    public void deleteCommentFromAdmin(Long commentId) {
        commentRepository.deleteById(commentId);
    }

    private void isUserExists(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("Пользователь не найден.");
        }
    }
}
