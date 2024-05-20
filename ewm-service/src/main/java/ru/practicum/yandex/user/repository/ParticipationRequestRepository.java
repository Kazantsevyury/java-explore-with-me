package ru.practicum.yandex.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.yandex.user.model.ParticipationRequest;

import java.util.List;
import java.util.Optional;

public interface ParticipationRequestRepository extends JpaRepository<ParticipationRequest, Long> {

    @Query("SELECT p FROM ParticipationRequest p JOIN FETCH p.requester r JOIN FETCH p.event e WHERE r.id = :requesterId AND e.id = :eventId")
    Optional<ParticipationRequest> findByRequesterIdAndEventId(Long requesterId, Long eventId);

    @Query("SELECT p FROM ParticipationRequest p JOIN FETCH p.requester r JOIN FETCH p.event e WHERE r.id = :requesterId")
    List<ParticipationRequest> findAllByRequesterId(Long requesterId);

    @Query("SELECT p FROM ParticipationRequest p JOIN FETCH p.requester r JOIN FETCH p.event e WHERE e.id = :eventId")
    List<ParticipationRequest> findAllByEventId(Long eventId);

    @Query("SELECT p FROM ParticipationRequest p JOIN FETCH p.requester r JOIN FETCH p.event e WHERE p.id IN :requestIds")
    List<ParticipationRequest> findAllByIdIn(List<Long> requestIds);
}
