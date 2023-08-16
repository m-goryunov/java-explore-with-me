package ru.practicum.comment.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommentCountDto {
    private Long eventId;
    private Long commentCount;
}
