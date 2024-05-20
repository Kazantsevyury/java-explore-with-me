package ru.practicum.yandex.compilation.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.yandex.compilation.dto.CompilationDto;
import ru.practicum.yandex.compilation.dto.NewCompilationDto;
import ru.practicum.yandex.compilation.dto.UpdateCompilationRequest;
import ru.practicum.yandex.compilation.mapper.CompilationMapper;
import ru.practicum.yandex.compilation.model.Compilation;
import ru.practicum.yandex.compilation.service.CompilationService;

import javax.validation.Valid;

/**
 * Административный API для подборок
 */
@RestController
@RequestMapping("/admin/compilations")
@RequiredArgsConstructor
@Slf4j
public class CompilationAdminController {

    private final CompilationService compilationService;
    private final CompilationMapper compilationMapper;

    /**
     * Добавление новой подборки событий. Если добавлено успешно, возвращает статус ответа 201.
     *
     * @param newCompilationDto параметры новой подборки
     * @return добавленная подборка
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CompilationDto addCompilation(@RequestBody @Valid NewCompilationDto newCompilationDto) {
        log.info("Добавление новой подборки: '{}'.", newCompilationDto);
        Compilation compilation = compilationService.addCompilation(newCompilationDto);
        return compilationMapper.toDto(compilation);
    }

    /**
     * Обновление параметров подборки событий.
     *
     * @param compId         идентификатор подборки для обновления
     * @param updateRequest  параметры обновления
     * @return обновленная подборка
     */
    @PatchMapping("/{compId}")
    public CompilationDto updateCompilation(@PathVariable Long compId,
                                            @RequestBody @Valid UpdateCompilationRequest updateRequest) {
        log.info("Обновление подборки с id '{}.", compId);
        Compilation compilation = compilationService.updateCompilation(compId, updateRequest);
        return compilationMapper.toDto(compilation);
    }

    /**
     * Удаление подборки по идентификатору подборки. Если удалено успешно, возвращает статус ответа 204.
     *
     * @param compId идентификатор подборки для удаления
     */
    @DeleteMapping("/{compId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCompilation(@PathVariable Long compId) {
        log.info("Удаление подборки с id '{}'.", compId);
        compilationService.deleteCompilation(compId);
    }
}
