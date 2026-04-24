package by.lobacevich.gateway.client;

import by.lobacevich.gateway.dto.ValidateRequestDto;
import by.lobacevich.gateway.dto.ValidateResponseDto;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import reactor.core.publisher.Mono;

@Log4j2
@RequiredArgsConstructor
@Component
public class AuthClient extends AbstractWebClient {

    private final WebClient authWebClient;

    private static final String SERVICE_NAME = "Auth service";

    @CircuitBreaker(name = "authService")
    public Mono<ValidateResponseDto> validateToken(String token) {
        return authWebClient.post()
                .uri("/auth/validate")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(new ValidateRequestDto(token))
                .exchangeToMono(response -> handleResponse(response,
                        ValidateResponseDto.class)
                )
                .onErrorMap(WebClientRequestException.class,
                        e -> onConnectionError(SERVICE_NAME));
    }
}
