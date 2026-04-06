package by.lobacevich.gateway.filter;

import by.lobacevich.gateway.security.UserPrincipal;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class AuthHeadersFilter implements GlobalFilter {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        return ReactiveSecurityContextHolder.getContext()
                .map(SecurityContext::getAuthentication)
                .flatMap(auth -> {
                    if (auth == null) {
                        return chain.filter(exchange);
                    }
                    ServerHttpRequest mutated = exchange.getRequest().mutate()
                            .header("X-User-Id", ((UserPrincipal) auth.getPrincipal()).userId().toString())
                            .header("X-Role", auth.getAuthorities().iterator().next().getAuthority())
                            .build();
                    return chain.filter(exchange.mutate().request(mutated).build());
                });
    }
}
