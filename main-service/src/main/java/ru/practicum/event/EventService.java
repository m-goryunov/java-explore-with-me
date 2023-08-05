package ru.practicum.event;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.EndpointHitDto;
import ru.practicum.StatsClient;
import ru.practicum.ViewStatsDto;
import ru.practicum.category.Category;
import ru.practicum.category.CategoryRepository;
import ru.practicum.category.CategoryService;
import ru.practicum.category.dto.CategoryMapper;
import ru.practicum.event.dto.*;
import ru.practicum.event.util.State;
import ru.practicum.event.util.StateActionAdmin;
import ru.practicum.event.util.StateActionPrivate;
import ru.practicum.exception.ForbiddenException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.exception.ValidationException;
import ru.practicum.location.Location;
import ru.practicum.location.LocationMapper;
import ru.practicum.location.LocationRepository;
import ru.practicum.request.RequestRepository;
import ru.practicum.request.dto.ConfirmedRequestsDto;
import ru.practicum.user.User;
import ru.practicum.user.UserRepository;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static ru.practicum.event.util.State.PENDING;
import static ru.practicum.event.util.State.PUBLISHED;
import static ru.practicum.event.util.StateActionAdmin.PUBLISH_EVENT;
import static ru.practicum.event.util.StateActionAdmin.REJECT_EVENT;
import static ru.practicum.event.util.StateActionPrivate.CANCEL_REVIEW;
import static ru.practicum.event.util.StateActionPrivate.SEND_TO_REVIEW;
import static ru.practicum.request.util.RequestStatus.CONFIRMED;

@Service
@RequiredArgsConstructor
@Transactional
@FieldDefaults(level = AccessLevel.PRIVATE)
public class EventService {
    final EventRepository eventRepository;
    final UserRepository userRepository;
    final CategoryRepository categoryRepository;
    final CategoryService categoryService;
    final LocationRepository locationRepository;
    final RequestRepository requestRepository;
    final StatsClient statsClient;

    public EventDto addEvent(Long userId, NewEventDto newEventDto) {
        checkActualTime(newEventDto.getEventDate());
        User user = userRepository.findById(userId).orElseThrow(() ->
                new NotFoundException("Пользователь не найден."));
        Long catId = newEventDto.getCategory();
        Category category = categoryRepository.findById(catId).orElseThrow(() ->
                new NotFoundException("Категория не найдена."));
        Location location = checkLocation(LocationMapper.toLocation(newEventDto.getLocation()));
        Event event = EventMapper.toEvent(newEventDto);
        event.setInitiator(user);
        event.setCategory(category);
        event.setLocation(location);
        event.setCreatedOn(LocalDateTime.now());
        event.setState(PENDING);
        return EventMapper.toEventFullDto(eventRepository.save(event), 0L);
    }

    public EventDto updateEventByOwner(Long userId, Long eventId, UpdateEventUserRequestDto updateEvent) {
        Event event = getEvent(eventId, userId);
        if (event.getState() == PUBLISHED) {
            throw new ForbiddenException("Нельзя обновить опубликованное событие.");
        }
        String annotation = updateEvent.getAnnotation();
        if (annotation != null) {
            event.setAnnotation(annotation);
        }
        if (updateEvent.getCategory() != null) {
            event.setCategory(CategoryMapper.toCategory(categoryService.getCategoryById(updateEvent.getCategory())));
        }
        String description = updateEvent.getDescription();
        if (description != null) {
            event.setDescription(description);
        }
        LocalDateTime eventDate = updateEvent.getEventDate();
        if (eventDate != null) {
            checkActualTime(eventDate);
            event.setEventDate(eventDate);
        }
        if (updateEvent.getLocation() != null) {
            Location location = checkLocation(LocationMapper.toLocation(updateEvent.getLocation()));
            event.setLocation(location);
        }
        if (updateEvent.getPaid() != null) {
            event.setPaid(updateEvent.getPaid());
        }
        if (updateEvent.getParticipantLimit() != null) {
            event.setParticipantLimit(updateEvent.getParticipantLimit());
        }
        if (updateEvent.getRequestModeration() != null) {
            event.setRequestModeration(updateEvent.getRequestModeration());
        }
        String title = updateEvent.getTitle();
        if (title != null) {
            event.setTitle(title);
        }
        if (updateEvent.getStateAction() != null) {
            StateActionPrivate stateActionPrivate = StateActionPrivate.valueOf(updateEvent.getStateAction());
            if (stateActionPrivate.equals(SEND_TO_REVIEW)) {
                event.setState(PENDING);
            } else if (stateActionPrivate.equals(CANCEL_REVIEW)) {
                event.setState(State.CANCELED);
            }
        }
        return EventMapper.toEventFullDto(eventRepository.save(event),
                requestRepository.countByEventIdAndStatus(eventId, CONFIRMED));
    }

    public EventDto updateEventByAdmin(Long eventId, UpdateEventAdminRequestDto updateEvent) {
        Event event = getEvent(eventId);
        if (updateEvent.getStateAction() != null) {
            StateActionAdmin stateAction = StateActionAdmin.valueOf(updateEvent.getStateAction());
            if (!event.getState().equals(PENDING) && stateAction.equals(PUBLISH_EVENT)) {
                throw new ForbiddenException("Событие в статусе PENDING");
            }
            if (event.getState().equals(PUBLISHED) && stateAction.equals(REJECT_EVENT)) {
                throw new ForbiddenException("Нельзя отменить уже опубликованное событие.");
            }
            if (stateAction.equals(PUBLISH_EVENT)) {
                event.setState(PUBLISHED);
                event.setPublishedOn(LocalDateTime.now());
            } else if (stateAction.equals(REJECT_EVENT)) {
                event.setState(State.CANCELED);
            }
        }
        String annotation = updateEvent.getAnnotation();
        if (annotation != null) {
            event.setAnnotation(annotation);
        }
        if (updateEvent.getCategory() != null) {
            event.setCategory(CategoryMapper.toCategory(categoryService.getCategoryById(updateEvent.getCategory())));
        }
        String description = updateEvent.getDescription();
        if (description != null) {
            event.setDescription(description);
        }
        LocalDateTime eventDate = updateEvent.getEventDate();
        if (eventDate != null) {
            checkActualTime(eventDate);
            event.setEventDate(eventDate);
        }
        if (updateEvent.getLocation() != null) {
            event.setLocation(checkLocation(LocationMapper.toLocation(updateEvent.getLocation())));
        }
        if (updateEvent.getPaid() != null) {
            event.setPaid(updateEvent.getPaid());
        }
        if (updateEvent.getParticipantLimit() != null) {
            event.setParticipantLimit(updateEvent.getParticipantLimit());
        }
        if (updateEvent.getRequestModeration() != null) {
            event.setRequestModeration(updateEvent.getRequestModeration());
        }
        String title = updateEvent.getTitle();
        if (title != null) {
            event.setTitle(title);
        }
        return EventMapper.toEventFullDto(eventRepository.save(event),
                requestRepository.countByEventIdAndStatus(eventId, CONFIRMED));
    }

    @Transactional(readOnly = true)
    public List<EventShortDto> getEventsByOwner(Long userId, Integer from, Integer size) {
        List<Event> events = eventRepository.findAllByInitiatorId(userId, PageRequest.of(from / size, size));
        List<Long> ids = events.stream().map(Event::getId).collect(Collectors.toList());
        Map<Long, Long> confirmedRequests = requestRepository.findAllByEventIdInAndStatus(ids, CONFIRMED)
                .stream()
                .collect(Collectors.toMap(ConfirmedRequestsDto::getEvent, ConfirmedRequestsDto::getCount));
        return events.stream()
                .map(event -> EventMapper.toEventShortDto(event, confirmedRequests.getOrDefault(event.getId(), 0L)))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public EventDto getEventByOwner(Long userId, Long eventId) {
        return EventMapper.toEventFullDto(getEvent(eventId, userId),
                requestRepository.countByEventIdAndStatus(eventId, CONFIRMED));
    }

    @Transactional(readOnly = true)
    public List<EventWithViewsDto> getEventsByAdminParams(List<Long> users, List<String> states, List<Long> categories,
                                                          LocalDateTime rangeStart, LocalDateTime rangeEnd,
                                                          Integer from, Integer size) {
        if (rangeStart != null && rangeEnd != null && rangeStart.isAfter(rangeEnd)) {
            throw new ValidationException("Ошибка валидации.");
        }
        Specification<Event> specification = Specification.where(null);
        if (users != null) {
            specification = specification.and((root, query, criteriaBuilder) ->
                    root.get("initiator").get("id").in(users));
        }
        if (states != null) {
            specification = specification.and((root, query, criteriaBuilder) ->
                    root.get("state").as(String.class).in(states));
        }
        if (categories != null) {
            specification = specification.and((root, query, criteriaBuilder) ->
                    root.get("category").get("id").in(categories));
        }
        if (rangeStart != null) {
            specification = specification.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.greaterThanOrEqualTo(root.get("eventDate"), rangeStart));
        }
        if (rangeEnd != null) {
            specification = specification.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.lessThanOrEqualTo(root.get("eventDate"), rangeEnd));
        }
        List<Event> events = eventRepository.findAll(specification, PageRequest.of(from / size, size)).getContent();
        List<EventWithViewsDto> result = new ArrayList<>();
        List<String> uris = events.stream()
                .map(event -> String.format("/events/%s", event.getId()))
                .collect(Collectors.toList());

        LocalDateTime start = events.stream()
                .map(Event::getCreatedOn)
                .min(LocalDateTime::compareTo)
                .orElseThrow(() -> new NotFoundException("Дата старта не найдена."));

        ResponseEntity<Object> response = statsClient.getStats(start, LocalDateTime.now(), uris, true);
        List<Long> ids = events.stream().map(Event::getId).collect(Collectors.toList());

        Map<Long, Long> confirmedRequests = requestRepository.findAllByEventIdInAndStatus(ids, CONFIRMED).stream()
                .collect(Collectors.toMap(ConfirmedRequestsDto::getEvent, ConfirmedRequestsDto::getCount));

        for (Event event : events) {
            ObjectMapper mapper = new ObjectMapper();
            mapper.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
            List<ViewStatsDto> statsDto = mapper.convertValue(response.getBody(), new TypeReference<>() {
            });

            if (!statsDto.isEmpty()) {
                result.add(EventMapper.toEventFullDtoWithViews(event, statsDto.get(0).getHits(),
                        confirmedRequests.getOrDefault(event.getId(), 0L)));
            } else {
                result.add(EventMapper.toEventFullDtoWithViews(event, 0L,
                        confirmedRequests.getOrDefault(event.getId(), 0L)));
            }
        }
        return result;
    }

    @Transactional(readOnly = true)
    public List<EventShortWithViewsDto> getEvents(String text, List<Long> categories, Boolean paid, LocalDateTime rangeStart,
                                                  LocalDateTime rangeEnd, Boolean onlyAvailable, String sort, Integer from,
                                                  Integer size, HttpServletRequest request) {
        if (rangeStart != null && rangeEnd != null && rangeStart.isAfter(rangeEnd)) {
            throw new ValidationException("Дата старта не может быть после даты конца.");
        }
        Specification<Event> specification = Specification.where(null);
        if (text != null) {
            specification = specification.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.or(
                            criteriaBuilder.like(criteriaBuilder.lower(root.get("annotation")), "%" + text.toLowerCase() + "%"),
                            criteriaBuilder.like(criteriaBuilder.lower(root.get("description")), "%" + text.toLowerCase() + "%")
                    ));
        }
        if (categories != null) {
            specification = specification.and((root, query, criteriaBuilder) ->
                    root.get("category").get("id").in(categories));
        }
        if (paid != null) {
            specification = specification.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.equal(root.get("paid"), paid));
        }
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startDateTime = Objects.requireNonNullElse(rangeStart, now);
        specification = specification.and((root, query, criteriaBuilder) ->
                criteriaBuilder.greaterThan(root.get("eventDate"), startDateTime));
        if (rangeEnd != null) {
            specification = specification.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.lessThan(root.get("eventDate"), rangeEnd));
        }
        if (onlyAvailable != null && onlyAvailable) {
            specification = specification.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.greaterThanOrEqualTo(root.get("participantLimit"), 0));
        }
        specification = specification.and((root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("state"), PUBLISHED));
        PageRequest pageRequest;
        switch (sort) {
            case "EVENT_DATE":
                pageRequest = PageRequest.of(from / size, size, Sort.by("eventDate"));
                break;
            case "VIEWS":
                pageRequest = PageRequest.of(from / size, size, Sort.by("views").descending());
                break;
            default:
                throw new ValidationException("Unknown sort: " + sort);
        }
        List<Event> events = eventRepository.findAll(specification, pageRequest).getContent();
        List<EventShortWithViewsDto> result = new ArrayList<>();
        List<String> uris = events.stream()
                .map(event -> String.format("/events/%s", event.getId()))
                .collect(Collectors.toList());
        LocalDateTime start = events.stream()
                .map(Event::getCreatedOn)
                .min(LocalDateTime::compareTo)
                .orElseThrow(() -> new NotFoundException("Дата старта не найдена."));
        ResponseEntity<Object> response = statsClient.getStats(start, LocalDateTime.now(), uris, true);
        List<Long> ids = events.stream().map(Event::getId).collect(Collectors.toList());
        Map<Long, Long> confirmedRequests = requestRepository.findAllByEventIdInAndStatus(ids, CONFIRMED)
                .stream()
                .collect(Collectors.toMap(ConfirmedRequestsDto::getEvent, ConfirmedRequestsDto::getCount));
        for (Event event : events) {
            ObjectMapper mapper = new ObjectMapper();
            mapper.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true); //configure
            List<ViewStatsDto> statsDto = mapper.convertValue(response.getBody(), new TypeReference<>() {
            });
            if (!statsDto.isEmpty()) {
                result.add(EventMapper.toEventShortDtoWithViews(event, statsDto.get(0).getHits(),
                        confirmedRequests.getOrDefault(event.getId(), 0L)));
            } else {
                result.add(EventMapper.toEventShortDtoWithViews(event, 0L,
                        confirmedRequests.getOrDefault(event.getId(), 0L)));
            }
        }
        EndpointHitDto hit = EndpointHitDto.builder()
                .app("main-service")
                .uri(request.getRequestURI())
                .ip(request.getRemoteAddr())
                .timestamp(LocalDateTime.now())
                .build();
        statsClient.saveHit(hit);
        return result;
    }

    @Transactional(readOnly = true)
    public EventWithViewsDto getEventById(Long eventId, HttpServletRequest request) {
        Event event = getEvent(eventId);
        if (event.getState() != PUBLISHED) {
            throw new NotFoundException("Событие должно быть опубликовано.");
        }
        ResponseEntity<Object> response = statsClient.getStats(event.getCreatedOn(), LocalDateTime.now(),
                List.of(request.getRequestURI()), true);
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
        List<ViewStatsDto> statsDto = mapper.convertValue(response.getBody(), new TypeReference<>() {
        });
        EventWithViewsDto result;
        if (!statsDto.isEmpty()) {
            result = EventMapper.toEventFullDtoWithViews(event, statsDto.get(0).getHits(),
                    requestRepository.countByEventIdAndStatus(eventId, CONFIRMED));
        } else {
            result = EventMapper.toEventFullDtoWithViews(event, 0L,
                    requestRepository.countByEventIdAndStatus(eventId, CONFIRMED));
        }
        EndpointHitDto hit = EndpointHitDto.builder()
                .app("main-service")
                .uri(request.getRequestURI())
                .ip(request.getRemoteAddr())
                .timestamp(LocalDateTime.now())
                .build();

        statsClient.saveHit(hit);
        return result;
    }

    protected void checkActualTime(LocalDateTime eventTime) {
        if (eventTime.isBefore(LocalDateTime.now().plusHours(2))) {
            throw new ValidationException("Ошибка валидации.");
        }
    }

    private Event getEvent(Long eventId) {
        return eventRepository.findById(eventId).orElseThrow(() ->
                new NotFoundException("Событие не найдено."));
    }

    private Event getEvent(Long eventId, Long userId) {
        return eventRepository.findByIdAndInitiatorId(eventId, userId).orElseThrow(() ->
                new NotFoundException("Событие не найдено."));
    }

    private Location checkLocation(Location location) {
        if (locationRepository.existsByLatAndLon(location.getLat(), location.getLon())) {
            return locationRepository.findByLatAndLon(location.getLat(), location.getLon());
        } else {
            return locationRepository.save(location);
        }
    }
}
