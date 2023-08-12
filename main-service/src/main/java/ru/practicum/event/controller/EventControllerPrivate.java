package ru.practicum.event.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.event.EventService;
import ru.practicum.event.dto.*;
import ru.practicum.request.dto.ParticipationRequestDto;
import ru.practicum.request.dto.RequestMapper;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import java.util.List;

@Validated
@RestController
@RequestMapping("/users/{userId}/events")
@RequiredArgsConstructor
public class EventControllerPrivate {
    private final EventService eventService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EventFullDto saveEvent(@PathVariable(value = "userId") @Min(1) Long userId,
                                  @Valid @RequestBody NewEventDto input) {
        return EventMapper.toFullDto(eventService.saveEvent(userId, input));
    }

    @GetMapping
    public List<EventShortDto> getAllEventsByUserId(@PathVariable(value = "userId") @Min(1) Long userId,
                                                    @RequestParam(value = "from", required = false, defaultValue = "0") @Min(0) Integer from,
                                                    @RequestParam(value = "size", required = false, defaultValue = "10") @Min(1) Integer size) {
        final Pageable pageable = PageRequest.of(from / size, size, Sort.by(Sort.Direction.ASC, "id"));
        return EventMapper.toShortDto(eventService.getEventsByUserId(userId, pageable));
    }

    @GetMapping("/{eventId}")
    public EventFullDto getFullEventByOwner(@PathVariable(value = "userId") @Min(1) Long userId,
                                            @PathVariable(value = "eventId") @Min(1) Long eventId) {
        return EventMapper.toFullDto(eventService.getFullEventByOwner(userId, eventId));
    }

    @PatchMapping("/{eventId}")
    public EventFullDto updateEventByOwner(@PathVariable(value = "userId") @Min(0) Long userId,
                                           @PathVariable(value = "eventId") @Min(0) Long eventId,
                                           @Valid @RequestBody UpdateEventUserRequest inputUpdate) {
        return EventMapper.toFullDto(eventService.updateEventByOwner(userId, eventId, inputUpdate));
    }

    @GetMapping("/{eventId}/requests")
    public List<ParticipationRequestDto> getAllRequestByEventFromOwner(@PathVariable(value = "userId") @Min(1) Long userId,
                                                                       @PathVariable(value = "eventId") @Min(1) Long eventId) {
        return RequestMapper.toParticipationRequestDto(eventService.getAllRequestByEventFromOwner(userId, eventId));
    }

    @PatchMapping("/{eventId}/requests")
    public EventRequestStatusUpdateResult updateStatusRequestFromOwner(@PathVariable(value = "userId") @Min(1) Long userId,
                                                                       @PathVariable(value = "eventId") @Min(1) Long eventId,
                                                                       @RequestBody EventRequestStatusUpdateRequest inputUpdate) {
        return eventService.updateStatusRequestFromOwner(userId, eventId, inputUpdate);
    }
}