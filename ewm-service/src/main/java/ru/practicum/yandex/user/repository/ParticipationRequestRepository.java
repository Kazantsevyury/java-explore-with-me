package ru.practicum.yandex.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.yandex.user.model.ParticipationRequest;

import java.util.List;
import java.util.Optional;

public interface ParticipationRequestRepository extends JpaRepository<ParticipationRequest, Long> {

    @Query("SELECT p FROM ParticipationRequest p JOIN FETCH p.requester r JOIN FETCH p.event e WHERE r.id = ?1 AND e.id = ?2")
    Optional<ParticipationRequest> findByRequesterIdAndEventId(Long requesterId, Long eventId);

    @Query("SELECT p FROM ParticipationRequest p JOIN FETCH p.requester r JOIN FETCH p.event e WHERE r.id = ?1")
    List<ParticipationRequest> findAllByRequesterId(Long requesterId);

    @Query("SELECT p FROM ParticipationRequest p JOIN FETCH p.requester r JOIN FETCH p.event e WHERE e.id = ?1")
    List<ParticipationRequest> findAllByEventId(Long eventId);

    @Query("SELECT p FROM ParticipationRequest p JOIN FETCH p.requester r JOIN FETCH p.event e WHERE p.id IN ?1")
    List<ParticipationRequest> findAllByIdIn(List<Long> requestIds);
}
