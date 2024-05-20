package ru.practicum.yandex.events.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.yandex.events.model.Event;

import java.util.List;
import java.util.Optional;

public interface EventRepository extends JpaRepository<Event, Long>, JpaSpecificationExecutor<Event> {

    @Query("SELECT e FROM Event e JOIN FETCH e.category c JOIN FETCH e.initiator i JOIN FETCH e.location l LEFT JOIN FETCH e.comments cm WHERE i.id = :userId")
    List<Event> findEventsByUserId(Long userId, Pageable pageable);

    @Query("SELECT e FROM Event e JOIN FETCH e.category c JOIN FETCH e.initiator i JOIN FETCH e.location l LEFT JOIN FETCH e.comments cm WHERE e.id = :eventId")
    Optional<Event> findFullEventById(Long eventId);

    long countEventsByCategoryId(Long categoryId);
}
