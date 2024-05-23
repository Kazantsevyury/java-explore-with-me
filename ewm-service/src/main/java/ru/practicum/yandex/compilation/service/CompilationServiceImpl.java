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
     * Добавление новой подборки событий. Подборка может не содержать событий.
     *
     * @param newCompilationDto параметры новой подборки
     * @return добавленная подборка
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
        log.info("Сохранена подборка с id '{}'.", savedCompilation.getId());
        return savedCompilation;
    }

    /**
     * Обновление параметров подборки событий.
     *
     * @param compId         идентификатор подборки для обновления
     * @param updateRequest  параметры обновления
     * @return обновленная подборка
     */
    @Override
    @Transactional
    public Compilation updateCompilation(Long compId, UpdateCompilationRequest updateRequest) {
        Compilation compilation = getCompilationWithEvents(compId);
        updateCompilationIfNeeded(updateRequest, compilation);
        Compilation savedCompilation = compilationRepository.save(compilation);
        log.info("Обновлена подборка с id '{}'.", compId);
        return savedCompilation;
    }

    /**
     * Удаление подборки по идентификатору подборки. Если удалено успешно, возвращает статус ответа 204.
     *
     * @param compId идентификатор подборки для удаления
     */
    @Override
    @Transactional
    public void deleteCompilation(Long compId) {
        getCompilation(compId);
        compilationRepository.deleteById(compId);
        log.info("Подборка с id '{}' удалена.", compId);
    }

    /**
     * Поиск подборок событий. Если ничего не найдено в соответствии с фильтром поиска, возвращает пустой список.
     *
     * @param pinned поиск только закрепленных подборок событий
     * @param from   первая подборка событий для отображения (необязательно, значение по умолчанию 0)
     * @param size   количество подборок событий для отображения (необязательно, значение по умолчанию 10)
     * @return списки подборок событий
     */
    @Override
    public List<Compilation> findCompilations(Boolean pinned, Long from, Integer size) {
        List<Specification<Compilation>> specifications = searchFilterToSpecificationList(pinned);
        OffsetPageRequest pageRequest = OffsetPageRequest.of(from, size);
        List<Compilation> compilations = compilationRepository
                .findAll(specifications.stream().reduce(Specification::and).orElse(null), pageRequest).getContent();
        log.info("Запрос подборок с параметрами: pinned - '{}', from - '{}', size - '{}'. Размер списка - '{}'.",
                pinned, from, size, compilations.size());
        return compilations;
    }

    /**
     * Поиск подборки событий по идентификатору. Если ничего не найдено, возвращает NotFoundException.
     *
     * @param compId идентификатор подборки событий
     * @return найденная подборка
     */
    @Override
    public Compilation findCompilationById(Long compId) {
        Compilation compilation = getCompilationWithEvents(compId);
        log.info("Запрос подборки с id '{}'.", compId);
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
                .orElseThrow(() -> new NotFoundException("Подборка с id '" + compId + "' не найдена."));
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
                .orElseThrow(() -> new NotFoundException("Подборка с id '" + compId + "' не найдена."));
    }
}
