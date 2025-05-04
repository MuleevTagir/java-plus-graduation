package ru.practicum.event.repository;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.practicum.categories.model.Category;
import ru.practicum.event.model.Event;
import ru.practicum.event.model.enums.EventState;

import java.util.List;
import java.util.Optional;

@Repository
public interface EventRepository extends JpaRepository<Event, Long>, CustomizedEventStorage {

    @Query("select ev " +
            "from Event ev " +
            "where ev.initiator.id = :userId " +
            "order by ev.id desc")
    List<Event> findEventsOfUser(Long userId, PageRequest pageRequest);

    Optional<Event> findByIdAndInitiatorId(Long userId, Long eventId);

    Optional<Event> findByIdAndState(Long id, EventState state);

    Optional<Event> findByCategory(Category category);

    Boolean existsByCategory_Id(Long catId);

    @Query("SELECT e FROM Event AS e" +
            " WHERE e.id IN ?1")
    List<Event> getAllByIds(List<Long> eventIds);
}
