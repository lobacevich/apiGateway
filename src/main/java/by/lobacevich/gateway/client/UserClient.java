package by.lobacevich.gateway.client;

import by.lobacevich.gateway.dto.UserCreateRequestDto;
import by.lobacevich.gateway.dto.UserCreatedResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
@Component
public class UserClient extends AbstractWebClient {

    private static final String SERVICE_NAME = "User service";

    private final WebClient userWebClient;

    public Mono<UserCreatedResponseDto> create(UserCreateRequestDto createDto) {
        return userWebClient.post()
                .uri("/users")
                .header("X-User-Id", "0")
                .header("X-Role", "ROLE_ADMIN")
                .bodyValue(createDto)
                .exchangeToMono(response -> handleResponse(response,
                        UserCreatedResponseDto.class,
                        SERVICE_NAME
                ))
                .onErrorMap(WebClientRequestException.class,
                        e -> onConnectionError(SERVICE_NAME));

    }

    public Mono<Void> delete(Long userId) {
        return userWebClient.delete()
                .uri("/users/{id}", userId)
                .header("X-User-Id", "0")
                .header("X-Role", "ROLE_ADMIN")
                .exchangeToMono(response -> handleResponse(response,
                        Void.class,
                        SERVICE_NAME
                ))
                .onErrorMap(WebClientRequestException.class,
                        e -> onConnectionError(SERVICE_NAME));
    }
}
