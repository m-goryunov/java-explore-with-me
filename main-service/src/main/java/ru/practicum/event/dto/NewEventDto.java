package ru.practicum.event.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import ru.practicum.location.LocationDto;

import javax.validation.constraints.*;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class NewEventDto {

    @Size(min = 20, max = 2000)
    @NotBlank
    String annotation;

    @NotNull
    Long category;

    @Size(min = 20, max = 7000)
    @NotBlank
    String description;

    @NotNull
    @Future
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    LocalDateTime eventDate;

    @NotNull
    LocationDto location;

    Boolean paid = false;

    @PositiveOrZero
    int participantLimit = 0;

    Boolean requestModeration = true;

    @Size(min = 3, max = 120)
    @NotBlank
    String title;

    public Boolean getPaid() {
        return paid;
    }

    public Boolean getRequestModeration() {
        return requestModeration;
    }
}
