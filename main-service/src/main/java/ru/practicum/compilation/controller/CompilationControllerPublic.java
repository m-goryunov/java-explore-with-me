package ru.practicum.compilation.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.compilation.CompilationService;
import ru.practicum.compilation.dto.CompilationDto;
import ru.practicum.compilation.dto.CompilationMapper;

import javax.validation.constraints.Min;
import java.util.List;

@Validated
@RestController
@RequestMapping("/compilations")
@RequiredArgsConstructor
public class CompilationControllerPublic {
    private final CompilationService compilationService;

    @GetMapping
    public List<CompilationDto> getAllCompilations(@RequestParam(value = "pinned", required = false) boolean pinned,
                                                   @RequestParam(name = "from", defaultValue = "0") @Min(0) Integer from,
                                                   @RequestParam(name = "size", defaultValue = "10") @Min(1) Integer size) {
        final Pageable pageable = PageRequest.of(from / size, size, Sort.by(Sort.Direction.ASC, "id"));
        return CompilationMapper.toCompilationDto(compilationService.getAllCompilations(pinned, pageable));
    }

    @GetMapping("/{compilationId}")
    public CompilationDto getCompilationById(@PathVariable Long compilationId) {
        return CompilationMapper.toCompilationDto(compilationService.getCompilationById(compilationId));
    }
}
