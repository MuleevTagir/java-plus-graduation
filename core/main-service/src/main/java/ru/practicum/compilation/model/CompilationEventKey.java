package ru.practicum.compilation.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Embeddable
@Getter
@Setter
@EqualsAndHashCode
public class CompilationEventKey implements Serializable {

    @Column(name = "event_id")
    private Long eventId;

    @Column(name = "compilation_id")
    private Long compilationId;
}
