package ru.practicum.compilation.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import ru.practicum.event.model.Event;

@Entity
@Table(name = "compilation_events")
@Getter
@Setter
public class CompilationEvent {
    @EmbeddedId
    private CompilationEventKey id;

    @ManyToOne
    @MapsId("compilationId")
    @JoinColumn(name = "compilation_id")
    private Compilation compilation;

    @ManyToOne
    @MapsId("eventId")
    @JoinColumn(name = "event_id")
    private Event event;
}
