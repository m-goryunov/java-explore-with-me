package ru.practicum.event;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.event.util.State;

import java.util.List;
import java.util.Optional;

public interface EventRepository extends JpaRepository<Event, Long> {

    List<Event> findAllByInitiatorId(Long userId, Pageable pageable);

    List<Event> findAll(Specification<Event> specification, Pageable pageable);

    Optional<Event> findByInitiatorIdAndId(Long userId, Long eventId);

    Boolean existsByInitiatorIdAndId(Long userId, Long eventId);

    Optional<Event> findByIdAndState(Long eventId, State state);
}
