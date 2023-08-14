package ru.practicum.location;

import org.springframework.data.jpa.repository.JpaRepository;

public interface LocationRepository extends JpaRepository<Location, Long> {

    Boolean existsByLatAndLon(Float lat, Float lon);

    Location findByLatAndLon(Float lat, Float lon);
}
