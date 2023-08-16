package ru.practicum.comment.dto;

import lombok.experimental.UtilityClass;
import ru.practicum.comment.Comment;
import ru.practicum.event.dto.EventMapper;
import ru.practicum.user.dto.UserMapper;

import java.util.List;
import java.util.stream.Collectors;

@UtilityClass
public class CommentMapper {
    public Comment toModel(NewCommentDto input) {
        return Comment.builder()
                .text(input.getText())
                .build();
    }

    public CommentDto toDto(Comment comment) {
        return CommentDto.builder()
                .id(comment.getId())
                .event(EventMapper.toShortDto(comment.getEvent()))
                .author(UserMapper.toUserShortDto(comment.getAuthor()))
                .createdOn(comment.getCreatedDate())
                .text(comment.getText())
                .build();
    }

    public List<CommentDto> toDto(List<Comment> comments) {
        return comments.stream().map(CommentMapper::toDto).collect(Collectors.toList());
    }
}
