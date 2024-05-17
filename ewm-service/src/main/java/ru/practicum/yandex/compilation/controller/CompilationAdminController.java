package ru.practicum.yandex.compilation.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.yandex.compilation.dto.CompilationDto;
import ru.practicum.yandex.compilation.dto.NewCompilationDto;
import ru.practicum.yandex.compilation.dto.UpdateCompilationRequest;
import ru.practicum.yandex.compilation.mapper.CompilationMapper;
import ru.practicum.yandex.compilation.model.Compilation;
import ru.practicum.yandex.compilation.service.CompilationService;

import javax.validation.Valid;

/**
 * Admin API for compilations
 */
@RestController
@RequestMapping("/admin/compilations")
@RequiredArgsConstructor
@Slf4j
public class CompilationAdminController {

    private final CompilationService compilationService;

    private final CompilationMapper compilationMapper;

    /**
     * Add new event compilation. If added successfully, returns 201 response status.
     *
     * @param newCompilationDto new compilation parameters
     * @return added compilation
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CompilationDto addCompilation(@RequestBody @Valid NewCompilationDto newCompilationDto) {
        log.info("Adding new compilation: '{}'.", newCompilationDto);
        Compilation compilation = compilationService.addCompilation(newCompilationDto);
        return compilationMapper.toDto(compilation);
    }

    /**
     * Update event compilation parameters.
     *
     * @param compId        compilation id to update
     * @param updateRequest update parameters
     * @return updated compilation
     */
    @PatchMapping("/{compId}")
    public CompilationDto updateCompilation(@PathVariable Long compId,
                                            @RequestBody @Valid UpdateCompilationRequest updateRequest) {
        log.info("Updating compilation with id '{}.", compId);
        Compilation compilation = compilationService.updateCompilation(compId, updateRequest);
        return compilationMapper.toDto(compilation);
    }

    /**
     * Delete compilation by compilation id. If deleted successfully, returns 204 response status.
     *
     * @param compId compilation id to delete
     */
    @DeleteMapping("/{compId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCompilation(@PathVariable Long compId) {
        log.info("Deleting compilation with id '{}'.", compId);
        compilationService.deleteCompilation(compId);
    }
}
