package ru.practicum.request;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.event.Event;
import ru.practicum.event.EventRepository;
import ru.practicum.exception.ForbiddenException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.request.util.RequestStatus;
import ru.practicum.user.User;
import ru.practicum.user.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

import static ru.practicum.event.util.State.PUBLISHED;


@Service
@RequiredArgsConstructor
@Transactional
public class RequestService {
    private final RequestRepository requestRepository;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;

    @Transactional
    public ParticipationRequest saveRequest(Long userId, Long eventId) {
        User user = userRepository.findById(userId).orElseThrow(
                () -> new NotFoundException("Пользователь не найден."));
        Event event = eventRepository.findById(eventId).orElseThrow(
                () -> new NotFoundException("Событие не найдено."));
        LocalDateTime createdOn = LocalDateTime.now();
        validateRequest(event, userId, eventId);
        ParticipationRequest request = new ParticipationRequest();
        request.setCreated(createdOn);
        request.setRequester(user);
        request.setEvent(event);
        if (event.getRequestModeration()) {
            request.setStatus(RequestStatus.PENDING);
        } else request.setStatus(RequestStatus.CONFIRMED);
        requestRepository.save(request);
        if (event.getParticipantLimit() == 0) {
            request.setStatus(RequestStatus.CONFIRMED);
        }
        return request;
    }

    @Transactional(readOnly = true)
    public List<ParticipationRequest> getAllRequestsByUserId(Long userId) {
        userRepository.findById(userId).orElseThrow(() ->
                new NotFoundException("Пользователь не найден."));
        return requestRepository.findAllByRequesterId(userId);
    }

    @Transactional
    public ParticipationRequest cancelRequest(Long userId, Long requestId) {
        ParticipationRequest request = requestRepository.findByIdAndRequesterId(requestId, userId);
        request.setStatus(RequestStatus.CANCELED);
        return requestRepository.save(request);
    }

    private void validateRequest(Event event, Long userId, Long eventId) {
        if (event.getInitiator().getId().equals(userId)) {
            throw new ForbiddenException("Инициатор не может участвовать в событии.");
        }
        if (event.getParticipantLimit() > 0 && event.getParticipantLimit()
                <= requestRepository.countByEventIdAndStatus(eventId, RequestStatus.CONFIRMED)) {
            throw new ForbiddenException("Кол-во участников превышено.");
        }
        if (!event.getState().equals(PUBLISHED)) {
            throw new ForbiddenException("Событие не опубликовано.");
        }
        if (requestRepository.existsByEventIdAndRequesterId(eventId, userId)) {
            throw new ForbiddenException("Событие уже существует.");
        }
    }
}