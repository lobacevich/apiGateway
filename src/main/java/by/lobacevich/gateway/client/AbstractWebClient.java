package by.lobacevich.gateway.client;

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
        return response.bodyToMono(String.class)
                .defaultIfEmpty("Authentication service error")
                .flatMap(body -> {
                    return Mono.error(new ServiceException(body, response.statusCode()));
                });
    }

    protected Throwable onConnectionError(String serviceName) {
        return new ServiceUnavailableException(serviceName + " is not available now");
    }
}
