package by.lobacevich.gateway.security;

import by.lobacevich.gateway.dto.ErrorDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.server.ServerAuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Log4j2
@Component
public class AuthEntryPoint implements ServerAuthenticationEntryPoint {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public Mono<Void> commence(ServerWebExchange exchange, AuthenticationException ex) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
        log.warn("{}, {}, {}", ex.getMessage(), ex.getClass().getSimpleName(), ex.getStackTrace());

        return Mono.fromCallable(() -> {
                    byte[] bytes = objectMapper.writeValueAsBytes(new ErrorDto(ex.getMessage()));
                    return response.bufferFactory().wrap(bytes);
                })
                .flatMap(buffer -> response.writeWith(Mono.just(buffer)));
    }
}
