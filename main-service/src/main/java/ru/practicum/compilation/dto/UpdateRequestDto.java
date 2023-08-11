package ru.practicum.compilation.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Size;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateRequestDto {
    private Long id;
    private List<Long> events;
    private Boolean pinned;
    @Size(min = 1, max = 50)
    private String title;

    public Boolean getPinned() {
        return pinned;
    }
}
