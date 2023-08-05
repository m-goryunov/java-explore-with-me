package ru.practicum.event.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.event.EventService;
import ru.practicum.event.dto.EventDto;
import ru.practicum.event.dto.EventShortDto;
import ru.practicum.event.dto.NewEventDto;
import ru.practicum.event.dto.UpdateEventUserRequestDto;
import ru.practicum.request.RequestService;
import ru.practicum.request.dto.EventRequestStatusUpdateRequestDto;
import ru.practicum.request.dto.EventRequestStatusUpdateResultDto;
import ru.practicum.request.dto.ParticipationRequestDto;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.util.List;

@Validated
@RestController
@RequestMapping("/users/{userId}/events")
@RequiredArgsConstructor
public class EventControllerPrivate {
    private final EventService eventService;
    private final RequestService requestService;

    @PostMapping
    @ResponseStatus(value = HttpStatus.CREATED)
    public EventDto addEvent(@PathVariable Long userId, @RequestBody @Valid NewEventDto newEventDto) {
        return eventService.addEvent(userId, newEventDto);
    }

    @PatchMapping("/{eventId}")
    public EventDto updateEventByOwner(@PathVariable Long userId,
                                       @PathVariable Long eventId,
                                       @RequestBody @Valid UpdateEventUserRequestDto updateEvent) {
        return eventService.updateEventByOwner(userId, eventId, updateEvent);
    }

    @PatchMapping("/{eventId}/requests")
    public EventRequestStatusUpdateResultDto updateRequestsStatus(@PathVariable Long userId,
                                                                  @PathVariable Long eventId,
                                                                  @RequestBody EventRequestStatusUpdateRequestDto request) {
        return requestService.updateRequestsStatus(userId, eventId, request);
    }

    @GetMapping
    List<EventShortDto> getEventsByOwner(@PathVariable Long userId,
                                         @RequestParam(value = "from", defaultValue = "0") @PositiveOrZero Integer from,
                                         @RequestParam(value = "size", defaultValue = "10") @Positive Integer size) {
        return eventService.getEventsByOwner(userId, from, size);
    }

    @GetMapping("/{eventId}")
    public EventDto getEventByOwner(@PathVariable Long userId, @PathVariable Long eventId) {
        return eventService.getEventByOwner(userId, eventId);
    }

    @GetMapping("/{eventId}/requests")
    public List<ParticipationRequestDto> getRequestsByEventOwner(@PathVariable Long userId, @PathVariable Long eventId) {
        return requestService.getRequestsByEventOwner(userId, eventId);
    }
}