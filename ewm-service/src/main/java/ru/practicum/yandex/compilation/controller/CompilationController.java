package ru.practicum.yandex.compilation.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.yandex.compilation.dto.CompilationDto;
import ru.practicum.yandex.compilation.mapper.CompilationMapper;
import ru.practicum.yandex.compilation.model.Compilation;
import ru.practicum.yandex.compilation.service.CompilationService;

import java.util.List;

/**
 * Публичный API для подборок
 */
@RestController
@RequestMapping("/compilations")
@RequiredArgsConstructor
@Slf4j
public class CompilationController {

    private final CompilationService compilationService;
    private final CompilationMapper compilationMapper;

    /**
     * Поиск подборок событий. Если ничего не найдено в соответствии с фильтром поиска, возвращает пустой список.
     *
     * @param pinned поиск только закрепленных подборок событий
     * @param from   первая подборка событий для отображения (необязательно, значение по умолчанию 0)
     * @param size   количество подборок событий для отображения (необязательно, значение по умолчанию 10)
     * @return списки подборок событий
     */
    @GetMapping
    public List<CompilationDto> findCompilations(@RequestParam(required = false) Boolean pinned,
                                                 @RequestParam(defaultValue = "0") Long from,
                                                 @RequestParam(defaultValue = "10") Integer size) {
        log.info("Запрос подборок с параметрами: pinned - '{}', from - '{}', size - '{}'.", pinned, from, size);
        List<Compilation> compilations = compilationService.findCompilations(pinned, from, size);
        return compilationMapper.toDtoList(compilations);
    }

    /**
     * Поиск подборки событий по идентификатору. Если ничего не найдено, возвращает статус ответа 404.
     *
     * @param compId идентификатор подборки событий
     * @return найденная подборка событий
     */
    @GetMapping("/{compId}")
    public CompilationDto findCompilationById(@PathVariable Long compId) {
        log.info("Запрос подборки с id '{}'.", compId);
        Compilation compilation = compilationService.findCompilationById(compId);
        return compilationMapper.toDto(compilation);
    }
}
