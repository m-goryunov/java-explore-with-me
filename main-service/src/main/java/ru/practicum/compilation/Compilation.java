package ru.practicum.compilation;

import lombok.*;
import ru.practicum.event.Event;

import javax.persistence.*;
import java.util.List;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "compilations")
@Builder
public class Compilation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "title", nullable = false)
    private String title;
    @Column(name = "pinned")
    private Boolean pinned;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "compilation_event",
            joinColumns = @JoinColumn(name = "compilation_id"),
            inverseJoinColumns = @JoinColumn(name = "event_id"))
    private List<Event> events;
}
