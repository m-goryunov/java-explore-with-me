package ru.practicum.event.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
public class CaseUpdatedStatusDto {
    private List<Long> idsFromUpdateStatus;
    private List<Long> processedIds;
}
