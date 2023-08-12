package ru.practicum.event.dto;


import lombok.experimental.UtilityClass;
import ru.practicum.category.dto.CategoryMapper;
import ru.practicum.event.Event;
import ru.practicum.location.LocationMapper;
import ru.practicum.user.dto.UserMapper;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@UtilityClass
public class EventMapper {
    public static Event toEvent(NewEventDto newEventDto) {
        return Event.builder()
                .id(null)
                .annotation(newEventDto.getAnnotation())
                .confirmedRequests(null)
                .description(newEventDto.getDescription())
                .eventDate(newEventDto.getEventDate())
                .location(LocationMapper.toLocation(newEventDto.getLocation()))
                .paid(newEventDto.getPaid())
                .participantLimit(newEventDto.getParticipantLimit())
                .requestModeration(newEventDto.getRequestModeration())
                .title(newEventDto.getTitle())
                .build();
    }

    public static EventFullDto toFullDto(Event event) {
        return EventFullDto.builder()
                .id(event.getId())
                .annotation(event.getAnnotation())
                .category(CategoryMapper.toCategoryDto(event.getCategory()))
                .confirmedRequests(event.getConfirmedRequests())
                .createdOn(event.getCreatedOn())
                .description(event.getDescription())
                .eventDate(event.getEventDate())
                .initiator(UserMapper.toUserShortDto(event.getInitiator()))
                .location(LocationMapper.toLocationDto(event.getLocation()))
                .paid(event.getPaid())
                .participantLimit(event.getParticipantLimit())
                .publishedOn(event.getPublisherDate())
                .requestModeration(event.getRequestModeration())
                .state(event.getState())
                .title(event.getTitle())
                .views(event.getViews())
                .build();
    }

    public static EventShortDto toShortDto(Event event) {
        return EventShortDto.builder()
                .id(event.getId())
                .annotation(event.getAnnotation())
                .category(CategoryMapper.toCategoryDto(event.getCategory()))
                .confirmedRequests(event.getConfirmedRequests())
                .eventDate(event.getEventDate())
                .initiator(UserMapper.toUserShortDto(event.getInitiator()))
                .paid(event.getPaid())
                .title(event.getTitle())
                .views(event.getViews())
                .build();
    }

    public static List<EventShortDto> toShortDto(List<Event> events) {
        return events == null ?
                new ArrayList<>() :
                events.stream().map(EventMapper::toShortDto).collect(Collectors.toList());
    }

    public static List<EventFullDto> toFullDto(List<Event> events) {
        return events == null ?
                new ArrayList<>() :
                events.stream().map(EventMapper::toFullDto).collect(Collectors.toList());
    }
}