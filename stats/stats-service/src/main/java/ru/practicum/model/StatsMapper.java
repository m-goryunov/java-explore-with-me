package ru.practicum.model;

import lombok.experimental.UtilityClass;
import ru.practicum.EndpointHitDto;
import ru.practicum.ViewStatsDto;

import java.util.List;
import java.util.stream.Collectors;

@UtilityClass
public class StatsMapper {

    public EndpointHit fromHitDto(EndpointHitDto dto) {
        return EndpointHit.builder()
                .id(dto.getId())
                .uri(dto.getUri())
                .app(dto.getApp())
                .ip(dto.getIp())
                .timestamp(dto.getTimestamp())
                .build();
    }

    public EndpointHitDto toHitDto(EndpointHit hit) {
        return EndpointHitDto.builder()
                .id(hit.getId())
                .uri(hit.getUri())
                .app(hit.getApp())
                .ip(hit.getIp())
                .timestamp(hit.getTimestamp())
                .build();
    }

    public ViewStats fromViewStatsDto(ViewStatsDto dto) {
        return ViewStats.builder()
                .hits(dto.getHits())
                .uri(dto.getUri())
                .app(dto.getApp())
                .build();
    }

    public ViewStatsDto toViewStatsDto(ViewStats viewStats) {
        return ViewStatsDto.builder()
                .hits(viewStats.getHits())
                .uri(viewStats.getUri())
                .app(viewStats.getApp())
                .build();
    }

    public List<ViewStatsDto> toViewStatsDto(List<ViewStats> viewStats) {
        return viewStats.stream()
                .map(StatsMapper::toViewStatsDto)
                .collect(Collectors.toList());
    }

}
