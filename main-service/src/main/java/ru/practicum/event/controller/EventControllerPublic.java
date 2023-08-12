package ru.practicum.event.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.event.EventService;
import ru.practicum.event.dto.EventFullDto;
import ru.practicum.event.dto.EventMapper;
import ru.practicum.event.dto.EventShortDto;

import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.Min;
import java.time.LocalDateTime;
import java.util.List;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/events")
public class EventControllerPublic {
    private final EventService eventService;

    @GetMapping
    public List<EventShortDto> getAllEventsPublic(@RequestParam(value = "text", defaultValue = "") String text,
                                                  @RequestParam(required = false) List<Long> categories,
                                                  @RequestParam(defaultValue = "false") Boolean paid,
                                                  @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime rangeStart,
                                                  @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime rangeEnd,
                                                  @RequestParam(defaultValue = "false") Boolean onlyAvailable,
                                                  @RequestParam(value = "from", defaultValue = "0") @Min(0) Integer from,
                                                  @RequestParam(value = "size", defaultValue = "10") @Min(1) Integer size,
                                                  HttpServletRequest request) {
        final Pageable pageable = PageRequest.of(from / size, size);
        return EventMapper.toShortDto(eventService.getAllEventsPublic(text, categories, paid, rangeStart, rangeEnd, onlyAvailable, pageable, request));
    }

    @GetMapping("/{eventId}")
    public EventFullDto getEventById(@PathVariable Long eventId, HttpServletRequest request) {
        return eventService.getEventById(eventId, request);
    }
}
