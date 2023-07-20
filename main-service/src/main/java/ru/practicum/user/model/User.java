package ru.practicum.user.model;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Getter
@Setter
@Builder
@NoArgsConstructor
@Entity
@Table(name = "users")
public class User {
    @Id
    private Long id;
    private String name;
    private String email;
}
