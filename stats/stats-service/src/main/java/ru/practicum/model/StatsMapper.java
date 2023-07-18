package ru.practicum.model;

import ru.practicum.EndpointHitDto;
import ru.practicum.ViewStatsDto;

import java.util.List;
import java.util.stream.Collectors;

public class StatsMapper {

    public static EndpointHit fromHitDto(EndpointHitDto dto) {
        return EndpointHit.builder()
                .id(dto.getId())
                .uri(dto.getUri())
                .app(dto.getApp())
                .ip(dto.getIp())
                .timestamp(dto.getTimestamp())
                .build();
    }

    public static EndpointHitDto toHitDto(EndpointHit hit) {
        return EndpointHitDto.builder()
                .id(hit.getId())
                .uri(hit.getUri())
                .app(hit.getApp())
                .ip(hit.getIp())
                .timestamp(hit.getTimestamp())
                .build();
    }

    public static ViewStats fromViewStatsDto(ViewStatsDto dto) {
        return ViewStats.builder()
                .hits(dto.getHits())
                .uri(dto.getUri())
                .app(dto.getApp())
                .build();
    }

    public static ViewStatsDto toViewStatsDto(ViewStats viewStats) {
        return ViewStatsDto.builder()
                .hits(viewStats.getHits())
                .uri(viewStats.getUri())
                .app(viewStats.getApp())
                .build();
    }

    public static List<ViewStatsDto> toViewStatsDto(List<ViewStats> viewStats) {
        return viewStats.stream()
                .map(StatsMapper::toViewStatsDto)
                .collect(Collectors.toList());
    }

}
