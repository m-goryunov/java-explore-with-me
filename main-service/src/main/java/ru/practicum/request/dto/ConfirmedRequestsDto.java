package ru.practicum.request.dto;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ConfirmedRequestsDto {
    private Long count;
    private Long event;

}
