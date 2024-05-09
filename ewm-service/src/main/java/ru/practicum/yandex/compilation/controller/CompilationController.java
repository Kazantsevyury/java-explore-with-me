package ru.practicum.yandex.compilation.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.yandex.compilation.dto.CompilationDto;
import ru.practicum.yandex.compilation.mapper.CompilationMapper;
import ru.practicum.yandex.compilation.model.Compilation;
import ru.practicum.yandex.compilation.service.CompilationService;

import java.util.List;

/**
 * Public API for compilations
 */
@RestController
@RequestMapping("/compilations")
@RequiredArgsConstructor
@Slf4j
public class CompilationController {

    private final CompilationService compilationService;

    private final CompilationMapper compilationMapper;

    /**
     * Find event compilations. If nothing was found according to search filter, returns empty list.
     *
     * @param pinned search only pinned event compilations
     * @param from   first event compilation to display (not required, default value 0)
     * @param size   number of event compilations to display (not required, default value 10)
     * @return lists of event compilations
     */
    @GetMapping
    public List<CompilationDto> findCompilations(@RequestParam(required = false) Boolean pinned,
                                                 @RequestParam(defaultValue = "0") Long from,
                                                 @RequestParam(defaultValue = "10") Integer size) {
        log.info("Requesting compilations with params: pinned - '{}', from - '{}', size - '{}'.", pinned, from, size);
        List<Compilation> compilations = compilationService.findCompilations(pinned, from, size);
        return compilationMapper.toDtoList(compilations);
    }

    /**
     * Find event compilation by id. If nothing found, return 404 response status.
     *
     * @param compId event compilation id.
     * @return found event compilation
     */
    @GetMapping("/{compId}")
    public CompilationDto findCompilationById(@PathVariable Long compId) {
        log.info("Requesting compilation with id '{}'.", compId);
        Compilation compilation = compilationService.findCompilationById(compId);
        return compilationMapper.toDto(compilation);
    }
}
