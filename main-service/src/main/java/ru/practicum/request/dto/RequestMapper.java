package ru.practicum.request.dto;

import lombok.experimental.UtilityClass;
import ru.practicum.request.ParticipationRequest;

@UtilityClass
public class RequestMapper {
    public ParticipationRequestDto toParticipationRequestDto(ParticipationRequest participationRequest) {
        return new ParticipationRequestDto(
                participationRequest.getId(),
                participationRequest.getCreated(),
                participationRequest.getEvent().getId(),
                participationRequest.getRequester().getId(),
                participationRequest.getStatus()
        );
    }
}