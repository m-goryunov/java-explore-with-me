package ru.practicum.request;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.request.dto.ParticipationRequestDto;
import ru.practicum.request.dto.RequestMapper;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/users/{userId}/requests")
public class RequestController {
    private final RequestService requestService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ParticipationRequestDto saveRequest(@PathVariable Long userId, @RequestParam Long eventId) {
        return RequestMapper.toParticipationRequestDto(requestService.saveRequest(userId, eventId));
    }

    @GetMapping
    public List<ParticipationRequestDto> getAllRequestsByUserId(@PathVariable Long userId) {
        return RequestMapper.toParticipationRequestDto(requestService.getAllRequestsByUserId(userId));
    }

    @PatchMapping("/{requestId}/cancel")
    public ParticipationRequestDto cancelRequest(@PathVariable Long userId, @PathVariable Long requestId) {
        return RequestMapper.toParticipationRequestDto(requestService.cancelRequest(userId, requestId));
    }
}