package ru.practicum.event;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.EndpointHitDto;
import ru.practicum.StatsClient;
import ru.practicum.ViewStatsDto;
import ru.practicum.category.Category;
import ru.practicum.category.CategoryRepository;
import ru.practicum.event.dto.*;
import ru.practicum.exception.ForbiddenException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.exception.ValidationException;
import ru.practicum.location.Location;
import ru.practicum.location.LocationMapper;
import ru.practicum.location.LocationRepository;
import ru.practicum.request.ParticipationRequest;
import ru.practicum.request.RequestRepository;
import ru.practicum.request.dto.ConfirmedRequestsDto;
import ru.practicum.request.dto.RequestMapper;
import ru.practicum.request.util.RequestStatus;
import ru.practicum.user.User;
import ru.practicum.user.UserRepository;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static ru.practicum.event.util.State.*;
import static ru.practicum.event.util.StateActionAdmin.PUBLISH_EVENT;
import static ru.practicum.event.util.StateActionAdmin.REJECT_EVENT;

@Service
@RequiredArgsConstructor
@Transactional
@FieldDefaults(level = AccessLevel.PRIVATE)
public class EventService {
    final EventRepository eventRepository;
    final UserRepository userRepository;
    final CategoryRepository categoryRepository;
    final LocationRepository locationRepository;
    final RequestRepository requestRepository;
    final StatsClient statsClient;
    final ObjectMapper mapper = new ObjectMapper();

    @Value("${app}")
    String app;

    @Transactional
    public Event saveEvent(Long userId, NewEventDto eventDto) {
        User user = userRepository.findById(userId).orElseThrow(
                () -> new NotFoundException("Пользователь не найден."));
        isBeforeTwoHours(eventDto.getEventDate());
        LocalDateTime createdOn = LocalDateTime.now();
        Category category = getCategoryById(eventDto.getCategory());
        Event event = EventMapper.toEvent(eventDto);
        event.setCategory(category);
        event.setInitiator(user);
        event.setState(PENDING);
        event.setLocation(getLocation(LocationMapper.toLocation(eventDto.getLocation())));
        event.setCreatedOn(createdOn);
        return eventRepository.save(event);
    }

    public List<Event> getEventsByUserId(Long userId, Pageable pageable) {
        isUserExists(userId);
        return eventRepository.findAllByInitiatorId(userId, pageable);
    }

    public Event getFullEventByOwner(Long userId, Long eventId) {
        isUserExists(userId);
        return getEventByInitiatorAndEventId(userId, eventId);
    }

    public List<ParticipationRequest> getAllRequestByEventFromOwner(Long userId, Long eventId) {
        isUserExists(userId);
        isUserInitiatedEvent(userId, eventId);
        return requestRepository.findAllByEventId(eventId);
    }

    @Transactional
    public Event updateEventByOwner(Long userId, Long eventId, UpdateEventUserRequest update) {
        isUserExists(userId);
        int size = 0;
        Event event = getEventByInitiatorAndEventId(userId, eventId);
        if (update.getEventDate() != null) {
            LocalDateTime newDate = update.getEventDate();
            isBeforeTwoHours(newDate);
            event.setEventDate(newDate);
        }
        if (!event.getInitiator().getId().equals(userId)) {
            throw new ForbiddenException("Событие может обновить только владелец");
        }
        if (event.getState().equals(PUBLISHED)) {
            throw new ForbiddenException("Нельзя обновить событие в статусе Published");
        }
        if (update.getAnnotation() != null && !update.getAnnotation().isBlank()) {
            event.setAnnotation(update.getAnnotation());
        }
        if (update.getCategory() != null) {
            Category category = getCategoryById(update.getCategory());
            event.setCategory(category);
        }
        if (update.getDescription() != null && !update.getDescription().isBlank()) {
            event.setDescription(update.getDescription());
        }
        if (update.getLocation() != null) {
            event.setLocation(getLocation(LocationMapper.toLocation(update.getLocation())));
        }
        if (update.getParticipantLimit() != null) {
            event.setParticipantLimit(update.getParticipantLimit());
        }
        if (update.getRequestModeration() != null) {
            event.setRequestModeration(update.getRequestModeration());
        }
        if (update.getStateAction() != null) {
            switch (update.getStateAction()) {
                case SEND_TO_REVIEW:
                    event.setState(PENDING);
                    break;
                case CANCEL_REVIEW:
                    event.setState(CANCELED);
                    break;
            }
        }
        if (update.getTitle() != null && !update.getTitle().isBlank()) {
            event.setTitle(update.getTitle());
        }
        return event;
    }

    @Transactional
    public EventRequestStatusUpdateResult updateStatusRequestFromOwner(Long userId, Long eventId, EventRequestStatusUpdateRequest update) {
        isUserExists(userId);
        Event event = getEventByInitiatorAndEventId(userId, eventId);
        if (!event.getRequestModeration() || event.getParticipantLimit() == 0) {
            throw new ValidationException("Событию не требуется подтверждение.");
        }
        final RequestStatus status = update.getStatus();

        switch (status) {
            case CONFIRMED:
                event.setConfirmedRequests(requestRepository.countByEventIdAndStatus(eventId, RequestStatus.CONFIRMED));
                if (event.getParticipantLimit().equals(event.getConfirmedRequests())) {
                    throw new ForbiddenException("Лимит участия заполнен.");
                }
                CaseUpdatedStatusDto updatedStatusConfirmed = statusHandler(event, CaseUpdatedStatusDto.builder()
                        .idsFromUpdateStatus(update.getRequestIds()).build(), RequestStatus.CONFIRMED);

                List<ParticipationRequest> confirmedRequests = requestRepository.findAllById(updatedStatusConfirmed.getProcessedIds());
                List<ParticipationRequest> rejectedRequests = new ArrayList<>();
                if (updatedStatusConfirmed.getIdsFromUpdateStatus().size() != 0) {
                    List<Long> ids = updatedStatusConfirmed.getIdsFromUpdateStatus();
                    rejectedRequests = rejectOtherRequest(ids, eventId);
                }

                return EventRequestStatusUpdateResult.builder()
                        .confirmedRequests(confirmedRequests
                                .stream()
                                .map(RequestMapper::toParticipationRequestDto).collect(Collectors.toList()))
                        .rejectedRequests(rejectedRequests
                                .stream()
                                .map(RequestMapper::toParticipationRequestDto).collect(Collectors.toList()))
                        .build();
            case REJECTED:
                event.setConfirmedRequests(requestRepository.countByEventIdAndStatus(eventId, RequestStatus.CONFIRMED));
                if (event.getParticipantLimit().equals(event.getConfirmedRequests())) {
                    throw new ForbiddenException("Лимит участия заполнен.");
                }

                CaseUpdatedStatusDto updatedStatusReject = statusHandler(event, CaseUpdatedStatusDto.builder()
                        .idsFromUpdateStatus(update.getRequestIds()).build(), RequestStatus.REJECTED);
                List<ParticipationRequest> rejectRequest = requestRepository.findAllById(updatedStatusReject.getProcessedIds());

                return EventRequestStatusUpdateResult.builder()
                        .rejectedRequests(rejectRequest
                                .stream()
                                .map(RequestMapper::toParticipationRequestDto).collect(Collectors.toList()))
                        .build();
            default:
                throw new ForbiddenException("Некорректный статус: " + status);
        }
    }

    @Transactional
    public Event updateEventByEventIdFromAdmin(Long eventId, UpdateEventAdminRequest update) {
        Event event = eventRepository.findById(eventId).orElseThrow(
                () -> new NotFoundException("Событие не найдено."));
        if (event.getState().equals(PUBLISHED) || event.getState().equals(CANCELED)) {
            throw new ForbiddenException("Нельзя изменить завершенное событие.");
        }
        if (update.getAnnotation() != null && !update.getAnnotation().isBlank()) {
            event.setAnnotation(update.getAnnotation());
        }
        if (update.getCategory() != null) {
            Category category = getCategoryById(update.getCategory());
            event.setCategory(category);
        }
        if (update.getDescription() != null && !update.getDescription().isBlank()) {
            event.setDescription(update.getDescription());
        }
        if (update.getEventDate() != null) {
            isBeforeTwoHours(update.getEventDate());
            event.setEventDate(update.getEventDate());
        }
        if (update.getLocation() != null) {
            event.setLocation(getLocation(LocationMapper.toLocation(update.getLocation())));
        }
        if (update.getPaid() != null) {
            event.setPaid(update.getPaid());
        }
        if (update.getParticipantLimit() != null) {
            event.setParticipantLimit(update.getParticipantLimit());
        }
        if (update.getRequestModeration() != null) {
            event.setRequestModeration(update.getRequestModeration());
        }
        if (update.getStateAction() != null) {
            if (update.getStateAction().equals(PUBLISH_EVENT)) {
                event.setState(PUBLISHED);
                event.setPublisherDate(LocalDateTime.now());
            } else if (update.getStateAction().equals(REJECT_EVENT)) {
                event.setState(CANCELED);
            }
        }
        if (update.getTitle() != null && !update.getTitle().isBlank()) {
            event.setTitle(update.getTitle());
        }
        return event;
    }

    public List<Event> getAllEventForParamFromAdmin(List<Long> users,
                                                    List<String> states,
                                                    List<Long> categories,
                                                    LocalDateTime rangeStart,
                                                    LocalDateTime rangeEnd,
                                                    Pageable pageable) {
        if (rangeStart != null && rangeEnd != null && rangeStart.isAfter(rangeEnd)) {
            throw new ForbiddenException("Дата начала не может быть позже даты окончания.");
        }

        Specification<Event> spec = Specification.where(null);

        if (users != null && !users.isEmpty()) {
            spec = spec.and((root, query, criteriaBuilder) -> root.get("initiator").get("id").in(users));
        }
        if (states != null && !states.isEmpty()) {
            spec = spec.and((root, query, criteriaBuilder) -> root.get("state").as(String.class).in(states));
        }
        if (categories != null && !categories.isEmpty()) {
            spec = spec.and((root, query, criteriaBuilder) -> root.get("category").get("id").in(categories));
        }
        if (rangeEnd != null) {
            spec = spec.and((root, query, criteriaBuilder) -> criteriaBuilder.lessThanOrEqualTo(root.get("eventDate"), rangeEnd));
        }
        if (rangeStart != null) {
            spec = spec.and((root, query, criteriaBuilder) -> criteriaBuilder.greaterThanOrEqualTo(root.get("eventDate"), rangeStart));
        }

        List<Event> events = eventRepository.findAll(spec, pageable);

        Map<Long, Long> ids = getConfirmedRequestsList(events);

        for (Event event : events) {
            if (ids.get(event.getId()) != null) {
                event.setConfirmedRequests(Math.toIntExact(ids.get(event.getId())));
            }
        }

        return events;
    }

    private Map<Long, Long> getConfirmedRequestsList(List<Event> events) {

        if ((events == null) || (events.isEmpty())) {
            return new HashMap<>();
        }

        List<ConfirmedRequestsDto> confirmedRequests = requestRepository.countByEventIdInAndStatus(events.stream()
                .map(Event::getId)
                .collect(Collectors.toList()), RequestStatus.CONFIRMED);

/*        Map<Long, Long> ids = new HashMap<>();
        for (ConfirmedRequestsDto confirmedRequest : confirmedRequests) {
            ids.put(confirmedRequest.getEvent(), confirmedRequest.getCount());
        }*/

        Map<Long, Long> map = new HashMap<>();
        for (ConfirmedRequestsDto confirmedRequest : confirmedRequests) {
            if (map.put(confirmedRequest.getEvent(), confirmedRequest.getCount()) != null) {
                throw new IllegalStateException("Duplicate key");
            }
        }
        return map;

        //return ids;
    }

    public List<Event> getAllEventsPublic(String text, List<Long> categories, Boolean paid,
                                          LocalDateTime start, LocalDateTime end,
                                          Boolean onlyAvailable, Pageable pageable, HttpServletRequest request) {
        if (end != null && start != null) {
            if (end.isBefore(start)) {
                throw new ValidationException("Дата начала не может быть позднее даты окончания.");
            }
        }

        saveHit(request);

        Specification<Event> spec = Specification.where(null);

        if (text != null) {
            spec = spec.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.or(
                            criteriaBuilder.like(criteriaBuilder.lower(root.get("annotation")), "%" + text.toLowerCase() + "%"),
                            criteriaBuilder.like(criteriaBuilder.lower(root.get("description")), "%" + text.toLowerCase() + "%")
                    ));
        }

        if (categories != null && !categories.isEmpty()) {
            spec = spec.and((root, query, criteriaBuilder) ->
                    root.get("category").get("id").in(categories));
        }

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startDateTime = Objects.requireNonNullElseGet(start, () -> now);
        spec = spec.and((root, query, criteriaBuilder) ->
                criteriaBuilder.greaterThan(root.get("eventDate"), startDateTime));

        if (end != null) {
            spec = spec.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.lessThan(root.get("eventDate"), end));
        }

        if (onlyAvailable != null && onlyAvailable) {
            spec = spec.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.greaterThanOrEqualTo(root.get("participantLimit"), 0));
        }

        spec = spec.and((root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("state"), PUBLISHED));

        List<Event> events = eventRepository.findAll(spec, pageable);
        setViewsOfEvents(events);

        Map<Long, Long> ids = getConfirmedRequestsList(events);

        for (Event event : events) {
            if (ids.get(event.getId()) != null) {
                event.setConfirmedRequests(Math.toIntExact(ids.get(event.getId())));
            }
        }

        return events;
    }

    public EventFullDto getEventById(Long eventId, HttpServletRequest request) {
        Event event = eventRepository.findByIdAndState(eventId, PUBLISHED).orElseThrow(
                () -> new NotFoundException("Событие не найдено."));
        saveHit(request);
        setViewsOfEvents(List.of(event));
        event.setConfirmedRequests(requestRepository.countByEventIdAndStatus(eventId, RequestStatus.CONFIRMED));
        return EventMapper.toFullDto(event);
    }

    private CaseUpdatedStatusDto statusHandler(Event event, CaseUpdatedStatusDto caseUpdatedStatus, RequestStatus status) {
        List<ParticipationRequest> requests = new ArrayList<>();
        Long eventId = event.getId();
        List<Long> ids = caseUpdatedStatus.getIdsFromUpdateStatus();
        int idsSize = caseUpdatedStatus.getIdsFromUpdateStatus().size();
        List<Long> processedIds = new ArrayList<>();
        int freeRequest = event.getParticipantLimit() - event.getConfirmedRequests();
        for (int i = 0; i < idsSize; i++) {
            final ParticipationRequest request = getRequestById(eventId, ids.get(i));
            if (freeRequest == 0) {
                break;
            }
            request.setStatus(status);
            requests.add(request);
            Long confirmedId = request.getId();
            processedIds.add(confirmedId);
            freeRequest--;
        }
        requestRepository.saveAll(requests);
        eventRepository.save(event);
        caseUpdatedStatus.setIdsFromUpdateStatus(ids);
        caseUpdatedStatus.setProcessedIds(processedIds);
        return caseUpdatedStatus;
    }

    private List<ParticipationRequest> rejectOtherRequest(List<Long> ids, Long eventId) {
        int size = ids.size();
        for (int i = 0; i < size; i++) {
            ParticipationRequest request = getRequestById(eventId, ids.get(i));
            if (!request.getStatus().equals(RequestStatus.PENDING)) {
                break;
            }

            request.setStatus(RequestStatus.REJECTED);
            requestRepository.save(request);
        }
        return requestRepository.findAllById(ids);
    }

    private Location getLocation(Location location) {
        if (locationRepository.existsByLatAndLon(location.getLat(), location.getLon())) {
            return locationRepository.findByLatAndLon(location.getLat(), location.getLon());
        } else {
            return locationRepository.save(location);
        }
    }

    private void isUserExists(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("Пользователь не найден.");
        }
    }

    private void isBeforeTwoHours(LocalDateTime eventDateStarted) {
        final LocalDateTime twoHoursLater = LocalDateTime.now().plusHours(2);
        if (eventDateStarted.isBefore(twoHoursLater)) {
            throw new ValidationException("Время начала события не может быть позднее 2 часов после");
        }
    }

    private Event getEventByInitiatorAndEventId(Long userId, Long eventId) {
        return eventRepository.findByInitiatorIdAndId(userId, eventId).orElseThrow(
                () -> new NotFoundException("Событие и/или пользователь не найдены."));
    }

    private void setViewsOfEvents(List<Event> events) {
        List<String> uris = events.stream()
                .map(event -> String.format("/events/%s", event.getId()))
                .collect(Collectors.toList());

        Optional<LocalDateTime> start = events.stream()
                .map(Event::getCreatedOn)
                .min(LocalDateTime::compareTo);

        if (start.isPresent()) {
            ResponseEntity<Object> response = statsClient.getStats(start.get(), LocalDateTime.now(), uris, false);
            List<ViewStatsDto> viewStatsList = mapper.convertValue(response.getBody(), new TypeReference<>() {
            });
            for (Event event : events) {
                ViewStatsDto currentViewStats = viewStatsList.stream()
                        .filter(statsDto -> {
                            Long eventIdOfViewStats = Long.parseLong(statsDto.getUri().substring("/events/".length()));
                            return eventIdOfViewStats.equals(event.getId());
                        })
                        .findFirst()
                        .orElse(null);

                Long views = (currentViewStats != null) ? currentViewStats.getHits() : 0;
                event.setViews(views.intValue() - 2);
            }
        }
    }

    private Category getCategoryById(Long categoryId) {
        return categoryRepository.findById(categoryId).orElseThrow(
                () -> new NotFoundException("Категория не найдена."));
    }

    private void isUserInitiatedEvent(Long userId, Long eventId) {
        if (!eventRepository.existsByInitiatorIdAndId(userId, eventId)) {
            throw new ValidationException("Пользователь не является инициатором.");
        }
    }

    private ParticipationRequest getRequestById(Long eventId, Long reqId) {
        return requestRepository.findByEventIdAndId(eventId, reqId).orElseThrow(
                () -> new NotFoundException("Запрос и/или событие не найдено."));
    }

    private void saveHit(HttpServletRequest request) {
        statsClient.saveHit(EndpointHitDto.builder()
                .app(app)
                .uri(request.getRequestURI())
                .ip(request.getRemoteAddr())
                .timestamp(LocalDateTime.now())
                .build());
    }

}
