package by.lobacevich.gateway.service.impl;

import by.lobacevich.gateway.client.AuthClient;
import by.lobacevich.gateway.client.UserClient;
import by.lobacevich.gateway.dto.CreateFullRequestDto;
import by.lobacevich.gateway.dto.CreatedFullResponseDto;
import by.lobacevich.gateway.mapper.RegisterMapper;
import by.lobacevich.gateway.service.RegisterService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Log4j2
@RequiredArgsConstructor
@Service
public class RegisterServiceImpl implements RegisterService {

    private final AuthClient authClient;
    private final UserClient userClient;
    private final RegisterMapper mapper;

    @Override
    public Mono<CreatedFullResponseDto> registerUser(CreateFullRequestDto fullRequestDto) {

        return userClient.create(mapper.toUserCreateRequest(fullRequestDto))
                .flatMap(userCreated -> {
                    Long userId = userCreated.id();
                    log.info("User created in UserService with id: {}", userId);
                    return authClient.register(mapper.toAuthRegisterRequest(fullRequestDto, userId))
                            .flatMap(registerResponse -> {
                                CreatedFullResponseDto result = mapper.toResponseFull(userCreated, registerResponse);
                                log.info("User registered successfully: userId={}", userId);
                                return Mono.just(result);
                            })
                            .onErrorResume(e -> {
                                log.error("AuthService registration failed for userId={}, rolling back", userId, e);
                                return userClient.delete(userId)
                                        .doOnSuccess(unused ->
                                                log.info("User deleted successfully: userId={}", userId))
                                        .doOnError(deleteError ->
                                                log.error("Failed to delete user after rollback: userId={}",
                                                        userId, deleteError))
                                        .onErrorResume(deleteError -> Mono.empty())
                                        .then(Mono.error(e));
                            });
                });
    }
}
