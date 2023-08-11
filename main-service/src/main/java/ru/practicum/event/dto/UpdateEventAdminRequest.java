package ru.practicum.event.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;
import org.hibernate.validator.constraints.Length;
import ru.practicum.event.util.StateActionAdmin;
import ru.practicum.location.Location;

import javax.validation.constraints.Future;
import javax.validation.constraints.PositiveOrZero;
import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UpdateEventAdminRequest {
    @Length(min = 20, max = 2000)
    private String annotation;
    private Long category;
    @Length(min = 20, max = 7000)
    private String description;
    @Future
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime eventDate;
    private Location location;
    private Boolean paid;
    @PositiveOrZero
    private Integer participantLimit;
    private Boolean requestModeration;
    private StateActionAdmin stateAction;
    @Length(min = 3, max = 120)
    private String title;
}
