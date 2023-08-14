package ru.practicum.event;

import lombok.*;
import lombok.experimental.FieldDefaults;
import ru.practicum.category.Category;
import ru.practicum.event.util.State;
import ru.practicum.location.Location;
import ru.practicum.user.User;

import javax.persistence.*;
import java.time.LocalDateTime;

@Builder
@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "events")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;
    @Column(name = "annotation", nullable = false, length = 2000)
    String annotation;
    @ManyToOne(cascade = CascadeType.MERGE)
    @JoinColumn(name = "category_id")
    Category category;
    @Transient
    Integer confirmedRequests;
    @Column(name = "created_on")
    LocalDateTime createdOn;
    @Column(name = "description", length = 7000)
    String description;
    @Column(name = "event_date", nullable = false)
    LocalDateTime eventDate;
    @ManyToOne(cascade = CascadeType.MERGE)
    @JoinColumn(name = "initiator_id")
    User initiator;
    @ManyToOne
    @JoinColumn(name = "location_id", nullable = false)
    Location location;
    @Column(name = "paid")
    Boolean paid;
    @Column(name = "participant_limit")
    Integer participantLimit;
    @Column(name = "published_on")
    LocalDateTime publisherDate;
    @Column(name = "request_moderation")
    Boolean requestModeration;
    @Enumerated(EnumType.STRING)
    @Column(name = "state")
    State state;
    @Column(name = "title", nullable = false, length = 120)
    String title;
    @Transient
    Integer views;
}
