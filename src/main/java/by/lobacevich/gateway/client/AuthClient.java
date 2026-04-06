package by.lobacevich.gateway.client;

import by.lobacevich.gateway.dto.ErrorDto;
import by.lobacevich.gateway.dto.ValidateRequestDto;
import by.lobacevich.gateway.dto.ValidateResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Log4j2
@RequiredArgsConstructor
@Component
public class AuthClient {

    private final WebClient authWebClient;

    public Mono<ValidateResponseDto> validateToken(String token) {
        return authWebClient.post()
                .uri("/auth/validate")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(new ValidateRequestDto(token))
                .retrieve()
                .onStatus(HttpStatusCode::isError,
                        response -> response.bodyToMono(ErrorDto.class)
                                .flatMap(errorDto -> {
                                    log.error("Auth service error: status={}, body={}",
                                            response.statusCode(),
                                            errorDto);
                                    return Mono.error(new BadCredentialsException(errorDto.message()));
                                })
                )
                .bodyToMono(ValidateResponseDto.class);
    }
}
