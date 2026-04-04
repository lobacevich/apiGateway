package by.lobacevich.gateway.client;

import by.lobacevich.gateway.dto.AuthRequest;
import by.lobacevich.gateway.dto.AuthResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
public class AuthClient {

    private static final String baseUrl = "http://localhost:8081";

    public Mono<AuthResponse> validateToken(String token) {
        WebClient webClient = WebClient.builder()
                .baseUrl(baseUrl)
                .build();
        return webClient.post().uri("/auth/validate")
                .bodyValue(new AuthRequest(token))
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .retrieve()
                .bodyToMono(AuthResponse.class);
    }
}
