package ru.practicum.request;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.event.Event;
import ru.practicum.event.EventRepository;
import ru.practicum.exception.ForbiddenException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.exception.ValidationException;
import ru.practicum.request.dto.EventRequestStatusUpdateRequestDto;
import ru.practicum.request.dto.EventRequestStatusUpdateResultDto;
import ru.practicum.request.dto.ParticipationRequestDto;
import ru.practicum.request.dto.RequestMapper;
import ru.practicum.request.util.RequestStatus;
import ru.practicum.user.User;
import ru.practicum.user.UserRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static ru.practicum.event.util.State.PUBLISHED;
import static ru.practicum.request.util.RequestStatus.*;


@Service
@RequiredArgsConstructor
@Transactional
public class RequestService {
    private final RequestRepository requestRepository;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;

    public ParticipationRequestDto addRequest(Long userId, Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Событие не найдено."));
        User user = getUser(userId);
        if (requestRepository.existsByRequesterIdAndEventId(userId, eventId)) {
            throw new ForbiddenException("Запрос уже существует.");
        }
        if (userId.equals(event.getInitiator().getId())) {
            throw new ForbiddenException("Нельзя отправить запрос самому себе.");
        }
        if (!event.getState().equals(PUBLISHED)) {
            throw new ForbiddenException("Participation is possible only in published event.");
        }
        if (event.getParticipantLimit() != 0 && event.getParticipantLimit() <=
                requestRepository.countByEventIdAndStatus(eventId, CONFIRMED)) {
            throw new ForbiddenException("Превышен лимит участия.");
        }
        ParticipationRequest request = new ParticipationRequest();
        request.setCreated(LocalDateTime.now());
        request.setEvent(event);
        request.setRequester(user);

        if (event.getRequestModeration() && event.getParticipantLimit() != 0) {
            request.setStatus(PENDING);
        } else {
            request.setStatus(CONFIRMED);
        }
        return RequestMapper.toParticipationRequestDto(requestRepository.save(request));
    }

    public EventRequestStatusUpdateResultDto updateRequestsStatus(Long userId, Long eventId,
                                                                  EventRequestStatusUpdateRequestDto statusUpdateRequest) {
        User initiator = getUser(userId);
        Event event = eventRepository.findByIdAndInitiatorId(eventId, userId).orElseThrow(() ->
                new NotFoundException("Событие не найдено."));
        if (!event.getInitiator().equals(initiator)) {
            throw new ValidationException("Пользователь не является инициатором.");
        }
        long confirmedRequests = requestRepository.countByEventIdAndStatus(eventId, CONFIRMED);
        if (event.getParticipantLimit() > 0 && event.getParticipantLimit() <= confirmedRequests) {
            throw new ForbiddenException("Превышен лимит участия.");
        }
        List<ParticipationRequestDto> confirmed = new ArrayList<>();
        List<ParticipationRequestDto> rejected = new ArrayList<>();
        List<ParticipationRequest> requests = requestRepository.findAllByEventIdAndIdInAndStatus(eventId,
                statusUpdateRequest.getRequestIds(), PENDING);
        for (int i = 0; i < requests.size(); i++) {
            ParticipationRequest request = requests.get(i);
            if (statusUpdateRequest.getStatus() == REJECTED) {
                request.setStatus(REJECTED);
                rejected.add(RequestMapper.toParticipationRequestDto(request));
            }
            if (statusUpdateRequest.getStatus() == CONFIRMED && event.getParticipantLimit() > 0 &&
                    (confirmedRequests + i) < event.getParticipantLimit()) {
                request.setStatus(CONFIRMED);
                confirmed.add(RequestMapper.toParticipationRequestDto(request));
            } else {
                request.setStatus(REJECTED);
                rejected.add(RequestMapper.toParticipationRequestDto(request));
            }
        }
        return new EventRequestStatusUpdateResultDto(confirmed, rejected);
    }

    public ParticipationRequestDto cancelRequest(Long userId, Long requestId) {
        ParticipationRequest request = requestRepository.findByIdAndRequesterId(requestId, userId);
        request.setStatus(RequestStatus.CANCELED);
        return RequestMapper.toParticipationRequestDto(requestRepository.save(request));
    }

    @Transactional(readOnly = true)
    public List<ParticipationRequestDto> getRequestsByEventOwner(Long userId, Long eventId) {
        checkUser(userId);
        eventRepository.findByIdAndInitiatorId(eventId, userId).orElseThrow(() ->
                new NotFoundException("Событие не найдено."));
        return requestRepository.findAllByEventId(eventId).stream()
                .map(RequestMapper::toParticipationRequestDto).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ParticipationRequestDto> getRequestsByUser(Long userId) {
        checkUser(userId);
        return requestRepository.findAllByRequesterId(userId).stream()
                .map(RequestMapper::toParticipationRequestDto).collect(Collectors.toList());
    }

    private User getUser(Long userId) {
        return userRepository.findById(userId).orElseThrow(() ->
                new NotFoundException("Пользователь не найден."));
    }

    private void checkUser(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("Пользователь не найден.");
        }
    }
}