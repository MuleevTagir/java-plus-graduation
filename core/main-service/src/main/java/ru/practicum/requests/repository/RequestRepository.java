package ru.practicum.requests.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.practicum.requests.model.Request;

import java.util.List;

@Repository
public interface RequestRepository extends JpaRepository<Request, Long> {

    @Query("SELECT r FROM Request r " +
            "WHERE r.requester.id = :userId " +
            "AND r.event.initiator.id != :userId")
    List<Request> findByUserId(Long userId);

    @Query("SELECT r FROM Request r " +
            "WHERE r.event.id = :eventId")
    List<Request> findByEventId(Long eventId);

    @Query("SELECT r FROM Request r " +
            "WHERE r.event.id in :eventIds ")
    List<Request> findByEventIds(List<Long> eventIds);

    List<Request> findRequestByEventIdAndStatus(Long eventId, String status);

    @Query("SELECT COUNT(r) FROM Request r " +
            "WHERE r.event.id = :eventId " +
            "AND r.status in :statuses")
    Long countByEventAndStatuses(Long eventId, List<String> statuses);

    @Query("SELECT r FROM Request r " +
            "WHERE r.event.id in :eventIds " +
            "AND r.status = :status")
    List<Request> findByEventIdsAndStatus(List<Long> eventIds, String status);

    @Query("SELECT r FROM Request r " +
            "WHERE r.event.initiator.id = :userId")
    List<Request> findByEventInitiatorId(Long userId);
}