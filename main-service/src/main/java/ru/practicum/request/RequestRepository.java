package ru.practicum.request;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.request.dto.ConfirmedRequestsDto;
import ru.practicum.request.util.RequestStatus;

import java.util.List;
import java.util.Optional;

public interface RequestRepository extends JpaRepository<ParticipationRequest, Long> {
    ParticipationRequest findByIdAndRequesterId(Long requestId, Long userId);

    List<ParticipationRequest> findAllByEventId(Long eventId);

    List<ParticipationRequest> findAllByRequesterId(Long userId);

    Boolean existsByEventIdAndRequesterId(Long eventId, Long userId);

    Integer countByEventIdAndStatus(Long eventId, RequestStatus status);

    @Query(
            "SELECT new ru.practicum.request.dto.ConfirmedRequestsDto(p.event.id, COUNT(p.id)) " +
                    "FROM ParticipationRequest p " +
                    "WHERE (p.event.id IN :eventId) " +
                    "AND (p.status = :status) " +
                    "GROUP BY p.event.id"
    )
    List<ConfirmedRequestsDto> countByEventIdInAndStatus(List<Long> eventId, RequestStatus status);

    Optional<ParticipationRequest> findByEventIdAndId(Long eventId, Long id);
}
