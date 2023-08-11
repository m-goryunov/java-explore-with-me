package ru.practicum.compilation.dto;

import lombok.experimental.UtilityClass;
import ru.practicum.compilation.Compilation;

import java.util.List;
import java.util.stream.Collectors;

import static ru.practicum.event.dto.EventMapper.toShortDto;

@UtilityClass
public class CompilationMapper {

    public CompilationDto toCompilationDto(Compilation compilation) {
        return CompilationDto.builder()
                .id(compilation.getId())
                .events(toShortDto(compilation.getEvents()))
                .pinned(compilation.getPinned())
                .title(compilation.getTitle())
                .build();
    }

    public Compilation toCompilation(NewCompilationDto compilationDto) {
        return Compilation.builder()
                .pinned(compilationDto.getPinned())
                .title(compilationDto.getTitle())
                .build();
    }

    public Compilation toCompilation(UpdateRequestDto update) {
        return Compilation.builder()
                .id(update.getId())
                .pinned(update.getPinned())
                .title(update.getTitle())
                .build();
    }

    public List<CompilationDto> toCompilationDto(List<Compilation> comps) {
        return comps.stream().map(CompilationMapper::toCompilationDto).collect(Collectors.toList());
    }
}
