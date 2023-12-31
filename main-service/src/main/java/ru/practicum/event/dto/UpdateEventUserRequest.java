package ru.practicum.event.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;
import ru.practicum.event.util.StateActionPrivate;
import ru.practicum.location.LocationDto;

import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class UpdateEventUserRequest {
    @Length(min = 20, max = 2000)
    private String annotation;
    @Positive
    private Long category;
    @Length(min = 20, max = 7000)
    private String description;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime eventDate;
    private LocationDto location;
    @PositiveOrZero
    private Integer participantLimit;
    private Boolean requestModeration;
    private StateActionPrivate stateAction;
    @Length(min = 3, max = 120)
    private String title;
}
