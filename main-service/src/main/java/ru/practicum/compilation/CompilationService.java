package ru.practicum.compilation;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.compilation.dto.CompilationMapper;
import ru.practicum.compilation.dto.NewCompilationDto;
import ru.practicum.compilation.dto.UpdateRequestDto;
import ru.practicum.event.Event;
import ru.practicum.event.EventRepository;
import ru.practicum.exception.NotFoundException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
@Transactional
public class CompilationService {
    private final CompilationRepository compilationRepository;
    private final EventRepository eventRepository;

    @Transactional
    public Compilation saveCompilation(NewCompilationDto compilationDto) {
        Compilation compilation = CompilationMapper.toCompilation(compilationDto);
        if (compilation.getPinned() == null) {
            compilation.setPinned(false);
        }
        if (compilationDto.getEvents() != null) {
            List<Event> events = eventRepository.findAllById(compilationDto.getEvents());
            compilation.setEvents(events);
        } else {
            compilation.setEvents(new ArrayList<>());
        }
        return compilationRepository.save(compilation);
    }

    @Transactional
    public Compilation updateCompilation(Long compId, UpdateRequestDto updateRequestDto) {
        Compilation compilation = getCompilationById(compId);
        if (updateRequestDto.getEvents() != null) {
            compilation.setEvents(updateRequestDto.getEvents().stream()
                    .flatMap(ids -> eventRepository.findAllById(Collections.singleton(ids))
                            .stream())
                    .collect(Collectors.toList()));
        }
        compilation.setPinned(updateRequestDto.getPinned() != null ? updateRequestDto.getPinned() : compilation.getPinned());
        compilation.setTitle(updateRequestDto.getTitle() != null ? updateRequestDto.getTitle() : compilation.getTitle());
        return compilationRepository.save(compilation);
    }

    public void deleteCompilation(Long compilationId) {
        compilationRepository.findById(compilationId).orElseThrow(() ->
                new NotFoundException("Подборка не найдена."));
        compilationRepository.deleteById(compilationId);
    }

    @Transactional(readOnly = true)
    public List<Compilation> getAllCompilations(Boolean isPinned, Pageable pageable) {
        return compilationRepository.findAllByPinnedIs(isPinned, pageable);
    }

    @Transactional(readOnly = true)
    public Compilation getCompilationById(Long compilationId) {
        return compilationRepository.findById(compilationId).orElseThrow(() ->
                new NotFoundException("Подборка не найдена."));
    }
}
