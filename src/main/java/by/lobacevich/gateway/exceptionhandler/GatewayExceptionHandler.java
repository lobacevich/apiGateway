package by.lobacevich.gateway.exceptionhandler;

import by.lobacevich.gateway.exception.ServiceException;
import by.lobacevich.gateway.exception.ServiceUnavailableException;
import by.lobacevich.gateway.util.AnswerWriter;
import lombok.extern.log4j.Log4j2;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.stream.Collectors;

@Log4j2
@Component
@Order(-1)
public class GatewayExceptionHandler implements ErrorWebExceptionHandler {

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
        ServerHttpResponse response = exchange.getResponse();

        switch (ex) {
            case ServiceUnavailableException serviceUnavailableException -> {
                log.error("Not available service: {}, {}", ex.getMessage(), ex.getStackTrace());
                response.setStatusCode(HttpStatus.SERVICE_UNAVAILABLE);
                return AnswerWriter.write(response, ex.getMessage());
            }
            case ServiceException serviceException -> {
                log.error("Auth service error: {}, {}", ex.getMessage(), ex.getStackTrace());
                response.setStatusCode(serviceException.getStatusCode());
                return AnswerWriter.write(response, serviceException.getMessage());
            }
            case WebExchangeBindException bindEx -> {
                String errorMessage = bindEx.getFieldErrors().stream()
                        .map(error -> error.getField() + ": " + error.getDefaultMessage())
                        .collect(Collectors.joining(", "));
                log.error("Validation error: {}, {}", bindEx.getMessage(), bindEx.getStackTrace());
                response.setStatusCode(HttpStatus.BAD_REQUEST);
                return AnswerWriter.write(response, errorMessage);
            }
            default -> {
                log.error("{}, {}", ex.getMessage(), ex.getStackTrace());
                response.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);
                return AnswerWriter.write(response, ex.getMessage());
            }
        }
    }
}
