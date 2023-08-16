package ru.practicum.comment;

import lombok.*;
import ru.practicum.event.Event;
import ru.practicum.user.User;

import javax.persistence.*;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "comments")
@Builder
public class Comment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "text", length = 7000)
    private String text;
    @ManyToOne(cascade = CascadeType.MERGE)
    @JoinColumn(name = "author_id")
    private User author;
    @ManyToOne(cascade = CascadeType.MERGE)
    @JoinColumn(name = "event_id")
    private Event event;
    @Column(name = "created_date")
    private LocalDateTime createdDate;
}
