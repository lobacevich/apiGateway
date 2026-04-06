package by.lobacevich.gateway.security;

import by.lobacevich.gateway.client.AuthClient;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.List;

@RequiredArgsConstructor
@Component
public class AuthManager implements ReactiveAuthenticationManager {

    private final AuthClient authClient;

    @Override
    public Mono<Authentication> authenticate(Authentication authentication) {
        String token = (String) authentication.getCredentials();
        return authClient.validateToken(token)
                .map(dto -> (Authentication) new UsernamePasswordAuthenticationToken(
                            new UserPrincipal(dto.userId()),
                            null,
                            List.of(new SimpleGrantedAuthority(dto.role()))
                    )
                )
                .switchIfEmpty(Mono.error(new BadCredentialsException("Invalid token")));
    }
}
