package by.lobacevich.gateway.service;

import by.lobacevich.gateway.dto.CreatedFullResponseDto;
import by.lobacevich.gateway.dto.CreateFullRequestDto;
import reactor.core.publisher.Mono;

public interface RegisterService {

    Mono<CreatedFullResponseDto> registerUser(CreateFullRequestDto requestDto);
}
