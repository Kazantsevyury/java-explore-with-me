package ru.practicum.yandex.compilation.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.yandex.compilation.model.Compilation;

import java.util.Optional;

/**
 * Репозиторий для управления сущностями Compilation.
 */
public interface CompilationRepository extends JpaRepository<Compilation, Long>, JpaSpecificationExecutor<Compilation> {

    /**
     * Находит компиляцию по идентификатору с полной загрузкой связанных событий и их категорий и инициаторов.
     *
     * @param compId идентификатор компиляции
     * @return опционально компиляция
     */
    @Query("SELECT c FROM Compilation c " +
            "LEFT JOIN FETCH c.events e " +
            "LEFT JOIN FETCH e.category " +
            "LEFT JOIN FETCH e.initiator " +
            "WHERE c.id = ?1")
    Optional<Compilation> findCompilationWithEventById(Long compId);
}
