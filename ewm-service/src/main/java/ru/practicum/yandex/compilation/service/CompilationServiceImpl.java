package ru.practicum.yandex.compilation.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.yandex.compilation.dto.NewCompilationDto;
import ru.practicum.yandex.compilation.dto.UpdateCompilationRequest;
import ru.practicum.yandex.compilation.model.Compilation;
import ru.practicum.yandex.compilation.repository.CompilationRepository;
import ru.practicum.yandex.events.model.Event;
import ru.practicum.yandex.events.repository.EventRepository;
import ru.practicum.yandex.shared.OffsetPageRequest;
import ru.practicum.yandex.shared.exception.NotFoundException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CompilationServiceImpl implements CompilationService {

    private final CompilationRepository compilationRepository;

    private final EventRepository eventRepository;

    /**
     * Add new event compilation. Compilation can have no events.
     *
     * @param newCompilationDto new compilation parameters
     * @return added compilation
     */
    @Override
    @Transactional
    public Compilation addCompilation(NewCompilationDto newCompilationDto) {
        List<Long> compilationEventIds = newCompilationDto.getEvents();
        List<Event> compilationEvents = getCompilationEvents(newCompilationDto, compilationEventIds);
        Compilation compilation = Compilation.builder()
                .title(newCompilationDto.getTitle())
                .pinned(newCompilationDto.isPinned())
                .events(compilationEvents)
                .build();
        Compilation savedCompilation = compilationRepository.save(compilation);
        log.info("Compilation with id '{}' was saved.", savedCompilation.getId());
        return savedCompilation;
    }

    /**
     * Update event compilation parameters.
     *
     * @param compId        compilation id to update
     * @param updateRequest update parameters
     * @return updated compilation
     */
    @Override
    @Transactional
    public Compilation updateCompilation(Long compId, UpdateCompilationRequest updateRequest) {
        Compilation compilation = getCompilationWithEvents(compId);
        updateCompilationIfNeeded(updateRequest, compilation);
        Compilation savedCompilation = compilationRepository.save(compilation);
        log.info("Compilation with id '{}' was updated.", compId);
        return savedCompilation;
    }

    /**
     * Delete compilation by compilation id. If deleted successfully, returns 204 response status.
     *
     * @param compId compilation id to delete
     */
    @Override
    @Transactional
    public void deleteCompilation(Long compId) {
        getCompilation(compId);
        compilationRepository.deleteById(compId);
        log.info("Compilation with id '{}' was deleted.", compId);
    }

    /**
     * Find event compilations. If nothing was found according to search filter, returns empty list.
     *
     * @param pinned search only pinned event compilations
     * @param from   first event compilation to display (not required, default value 0)
     * @param size   number of event compilations to display (not required, default value 10)
     * @return lists of event compilations
     */
    @Override
    public List<Compilation> findCompilations(Boolean pinned, Long from, Integer size) {
        List<Specification<Compilation>> specifications = searchFilterToSpecificationList(pinned);
        OffsetPageRequest pageRequest = OffsetPageRequest.of(from, size);
        List<Compilation> compilations = compilationRepository
                .findAll(specifications.stream().reduce(Specification::and).orElse(null), pageRequest).getContent();
        log.info("Requesting compilations, search filter: pinned - '{}', from - '{}', size - '{}'. List size - '{}'.",
                pinned, from, size, compilations.size());
        return compilations;
    }

    /**
     * Find event compilation by id. If nothing found, throws NotFoundException.
     *
     * @param compId event compilation id
     * @return found compilation
     */
    @Override
    public Compilation findCompilationById(Long compId) {
        Compilation compilation = getCompilationWithEvents(compId);
        log.info("Compilation with id '{}' was requested.", compId);
        return compilation;
    }

    private List<Specification<Compilation>> searchFilterToSpecificationList(Boolean pinned) {
        List<Specification<Compilation>> resultSpecification = new ArrayList<>();
        resultSpecification.add(pinned == null ? null : isPinned(pinned));
        return resultSpecification.stream().filter(Objects::nonNull).collect(Collectors.toList());

    }

    private Specification<Compilation> isPinned(Boolean pinned) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("pinned"), pinned);
    }

    private void updateCompilationIfNeeded(UpdateCompilationRequest updateRequest, Compilation compilation) {
        if (updateRequest.getEvents() != null) {
            List<Event> events = eventRepository.findAllById(updateRequest.getEvents());
            bindEventsToCompilation(compilation, events);
            compilation.setEvents(events);
        }
        if (updateRequest.getTitle() != null) {
            compilation.setTitle(updateRequest.getTitle());
        }
        if (updateRequest.getPinned() != null) {
            compilation.setPinned(updateRequest.getPinned());
        }
    }

    private void bindEventsToCompilation(Compilation compilation, List<Event> events) {
        events.forEach(event -> event.addToCompilation(compilation));
        eventRepository.saveAll(events);
    }

    private Compilation getCompilation(Long compId) {
        return compilationRepository.findById(compId)
                .orElseThrow(() -> new NotFoundException("Compilation with id '" + compId + "' not found."));
    }

    private List<Event> getCompilationEvents(NewCompilationDto newCompilationDto, List<Long> compilationEventIds) {
        List<Event> compilationEvents;
        if (newCompilationDto.getEvents() != null) {
            compilationEvents = eventRepository.findAllById(compilationEventIds);
        } else {
            compilationEvents = Collections.emptyList();
        }
        return compilationEvents;
    }

    private Compilation getCompilationWithEvents(Long compId) {
        return compilationRepository.findCompilationWithEventById(compId)
                .orElseThrow(() -> new NotFoundException("Compilation with id '" + compId + "' not found."));
    }
}
