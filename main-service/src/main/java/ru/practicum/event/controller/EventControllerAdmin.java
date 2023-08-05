package ru.practicum.event.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.event.EventService;
import ru.practicum.event.dto.EventDto;
import ru.practicum.event.dto.EventWithViewsDto;
import ru.practicum.event.dto.UpdateEventAdminRequestDto;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.time.LocalDateTime;
import java.util.List;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/events")
public class EventControllerAdmin {
    private final EventService eventService;

    @PatchMapping("/{eventId}")
    public EventDto updateEventByAdmin(@PathVariable Long eventId,
                                       @RequestBody @Valid UpdateEventAdminRequestDto updateEventAdminRequestDto) {
        return eventService.updateEventByAdmin(eventId, updateEventAdminRequestDto);
    }

    @GetMapping
    public List<EventWithViewsDto> getEventsByAdminParams(@RequestParam(required = false) List<Long> users,
                                                          @RequestParam(required = false) List<String> states,
                                                          @RequestParam(required = false) List<Long> categories,
                                                          @RequestParam(required = false) @DateTimeFormat(pattern =
                                                                  "yyyy-MM-dd HH:mm:ss") LocalDateTime rangeStart,
                                                          @RequestParam(required = false) @DateTimeFormat(pattern =
                                                                  "yyyy-MM-dd HH:mm:ss") LocalDateTime rangeEnd,
                                                          @RequestParam(value = "from", defaultValue = "0")
                                                          @PositiveOrZero Integer from,
                                                          @RequestParam(value = "size", defaultValue = "10")
                                                          @Positive Integer size) {
        return eventService.getEventsByAdminParams(users, states, categories, rangeStart, rangeEnd, from, size);
    }
}