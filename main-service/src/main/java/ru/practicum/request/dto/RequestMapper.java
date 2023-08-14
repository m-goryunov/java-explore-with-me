package ru.practicum.request.dto;

import lombok.experimental.UtilityClass;
import ru.practicum.request.ParticipationRequest;

import java.util.List;
import java.util.stream.Collectors;

@UtilityClass
public class RequestMapper {

    public ParticipationRequestDto toParticipationRequestDto(ParticipationRequest request) {
        return ParticipationRequestDto.builder()
                .id(request.getId())
                .event(request.getEvent().getId())
                .created(request.getCreated())
                .requester(request.getId())
                .status(request.getStatus())
                .build();
    }

    public ParticipationRequest toParticipationRequest(ParticipationRequestDto participationRequestDto) {
        return ParticipationRequest.builder()
                .id(participationRequestDto.getId())
                .event(null)
                .created(participationRequestDto.getCreated())
                .requester(null)
                .status(participationRequestDto.getStatus())
                .build();
    }

    public List<ParticipationRequestDto> toParticipationRequestDto(List<ParticipationRequest> requests) {
        return requests.stream().map(RequestMapper::toParticipationRequestDto).collect(Collectors.toList());
    }
}