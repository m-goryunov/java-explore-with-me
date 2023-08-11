package ru.practicum.request;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.request.util.RequestStatus;

import java.util.List;
import java.util.Optional;

public interface RequestRepository extends JpaRepository<ParticipationRequest, Long> {
    ParticipationRequest findByIdAndRequesterId(Long requestId, Long userId);

    List<ParticipationRequest> findAllByEventId(Long eventId);

    List<ParticipationRequest> findAllByRequesterId(Long userId);

    Boolean existsByEventIdAndRequesterId(Long eventId, Long userId);

    Integer countByEventIdAndStatus(Long eventId, RequestStatus status);

    Optional<ParticipationRequest> findByEventIdAndId(Long eventId, Long id);
}
