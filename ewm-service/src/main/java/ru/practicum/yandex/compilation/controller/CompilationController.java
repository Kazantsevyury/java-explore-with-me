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

@RestController
@RequestMapping("/compilations")
@RequiredArgsConstructor
@Slf4j
public class CompilationController {

    private final CompilationService compilationService;

    private final CompilationMapper compilationMapper;

    @GetMapping
    public List<CompilationDto> findCompilations(@RequestParam(required = false) Boolean pinned,
                                                 @RequestParam(defaultValue = "0") Long from,
                                                 @RequestParam(defaultValue = "10") Integer size) {
        log.info("Requesting compilations with params: pinned - '{}', from - '{}', size - '{}'.", pinned, from, size);
        List<Compilation> compilations = compilationService.findCompilations(pinned, from, size);
        return compilationMapper.toDtoList(compilations);
    }

    @GetMapping("/{compId}")
    public CompilationDto findCompilationById(@PathVariable Long compId) {
        log.info("Requesting compilation with id '{}'.", compId);
        Compilation compilation = compilationService.findCompilationById(compId);
        return compilationMapper.toDto(compilation);
    }
}
