package by.lobacevich.gateway.client;

import by.lobacevich.gateway.dto.ErrorDto;
import by.lobacevich.gateway.exception.ServiceException;
import by.lobacevich.gateway.exception.ServiceUnavailableException;
import lombok.extern.log4j.Log4j2;
import org.springframework.web.reactive.function.client.ClientResponse;
import reactor.core.publisher.Mono;

@Log4j2
public abstract class AbstractWebClient {

    protected <T> Mono<T> handleResponse(ClientResponse response,
                                         Class<T> dtoClass,
                                         String serviceName) {
        if (response.statusCode().is2xxSuccessful()) {
            return response.bodyToMono(dtoClass);
        }

        return response.bodyToMono(ErrorDto.class)
                .flatMap(errorDto -> {
                    log.error("{} error: status={}, body={}", serviceName, response.statusCode(), errorDto);
                    return Mono.error(new ServiceException(errorDto.message(), response.statusCode()));
                });
    }

    protected Throwable onConnectionError(String serviceName) {
        return new ServiceUnavailableException(serviceName + " is not available now");
    }
}
