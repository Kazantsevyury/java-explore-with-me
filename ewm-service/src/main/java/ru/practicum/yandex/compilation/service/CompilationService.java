package ru.practicum.yandex.compilation.service;

import ru.practicum.yandex.compilation.dto.NewCompilationDto;
import ru.practicum.yandex.compilation.dto.UpdateCompilationRequest;
import ru.practicum.yandex.compilation.model.Compilation;

import java.util.List;

public interface CompilationService {
    Compilation addCompilation(NewCompilationDto newCompilationDto);

    Compilation updateCompilation(Long compId, UpdateCompilationRequest updateRequest);

    void deleteCompilation(Long compId);

    List<Compilation> findCompilations(Boolean pinned, Long from, Integer size);

    Compilation findCompilationById(Long compId);
}
