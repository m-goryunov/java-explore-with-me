package ru.practicum.comment.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.comment.CommentService;
import ru.practicum.comment.dto.CommentDto;
import ru.practicum.comment.dto.CommentMapper;

import javax.validation.constraints.Min;
import java.util.List;

@RestController
@RequiredArgsConstructor
@Validated
@RequestMapping("/events/{eventId}/comments")
public class CommentControllerPublic {
    private final CommentService commentService;

    @GetMapping
    public List<CommentDto> getAllCommentByEvent(@PathVariable(value = "eventId") Long eventId,
                                                 @RequestParam(value = "from", defaultValue = "0") @Min(0) Integer from,
                                                 @RequestParam(value = "size", defaultValue = "10") @Min(1) Integer size) {
        return CommentMapper.toDto(commentService.getAllCommentsFromEvent(eventId, from, size));
    }
}