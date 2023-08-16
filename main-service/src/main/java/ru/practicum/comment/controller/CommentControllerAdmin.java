package ru.practicum.comment.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
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
@RequestMapping("/admin/comments")
public class CommentControllerAdmin {
    private final CommentService commentService;

    @GetMapping
    public List<CommentDto> searchComment(@RequestParam(value = "from", defaultValue = "0") @Min(0) Integer from,
                                          @RequestParam(value = "size", defaultValue = "10") @Min(1) Integer size,
                                          @RequestParam String text) {
        return CommentMapper.toDto(commentService.searchCommentByText(text, from, size));
    }

    @DeleteMapping("/{commentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteComment(@PathVariable(value = "commentId") Long commentId) {
        commentService.deleteCommentFromAdmin(commentId);
    }
}