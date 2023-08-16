package ru.practicum.comment.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.comment.CommentService;
import ru.practicum.comment.dto.CommentDto;
import ru.practicum.comment.dto.CommentMapper;
import ru.practicum.comment.dto.NewCommentDto;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import java.util.List;

@RestController
@RequiredArgsConstructor
@Validated
@RequestMapping("/users/{userId}/comments")
public class CommentControllerPrivate {
    private final CommentService commentService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CommentDto saveComment(@PathVariable(value = "userId") Long userId,
                                  @Valid @RequestBody NewCommentDto input,
                                  @RequestParam(value = "eventId") Long eventId) {
        return CommentMapper.toDto(commentService.saveComment(userId, eventId, input));
    }

    @GetMapping
    public List<CommentDto> getAllCommentByUser(@PathVariable(value = "userId") Long userId,
                                                @RequestParam(value = "from", defaultValue = "0") @Min(0) Integer from,
                                                @RequestParam(value = "size", defaultValue = "10") @Min(1) Integer size) {
        return CommentMapper.toDto(commentService.getAllCommentsFromUser(userId, from, size));
    }

    @PatchMapping("/{commentId}")
    public CommentDto updateComment(@PathVariable(value = "userId") Long userId,
                                    @PathVariable(value = "commentId") Long commentId,
                                    @Valid @RequestBody NewCommentDto update) {
        return CommentMapper.toDto(commentService.updateComment(userId, commentId, update));
    }

    @DeleteMapping("/{commentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteComment(@PathVariable(value = "userId") Long userId,
                              @PathVariable(value = "commentId") Long commentId) {
        commentService.deleteCommentFromOwner(userId, commentId);
    }
}
