package ru.practicum.compilation.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.compilation.CompilationService;
import ru.practicum.compilation.dto.CompilationDto;
import ru.practicum.compilation.dto.CompilationMapper;
import ru.practicum.compilation.dto.NewCompilationDto;
import ru.practicum.compilation.dto.UpdateRequestDto;

import javax.validation.Valid;

@RestController
@RequestMapping("/admin/compilations")
@RequiredArgsConstructor
public class CompilationControllerAdmin {
    private final CompilationService compilationService;

    @PostMapping
    @ResponseStatus(value = HttpStatus.CREATED)
    public CompilationDto saveCompilation(@RequestBody @Valid NewCompilationDto newCompilationDto) {
        return CompilationMapper.toCompilationDto(compilationService.saveCompilation(newCompilationDto));
    }

    @PatchMapping("/{compilationId}")
    public CompilationDto updateCompilation(@PathVariable Long compilationId,
                                            @RequestBody @Valid UpdateRequestDto updateCompilation) {
        return CompilationMapper.toCompilationDto(compilationService.updateCompilation(compilationId, updateCompilation));
    }

    @DeleteMapping("/{compilationId}")
    @ResponseStatus(value = HttpStatus.NO_CONTENT)
    public void deleteCompilation(@PathVariable long compilationId) {
        compilationService.deleteCompilation(compilationId);
    }
}
