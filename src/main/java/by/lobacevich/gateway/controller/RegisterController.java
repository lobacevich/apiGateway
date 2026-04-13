package by.lobacevich.gateway.controller;

import by.lobacevich.gateway.dto.CreatedFullResponseDto;
import by.lobacevich.gateway.dto.CreateFullRequestDto;
import by.lobacevich.gateway.service.RegisterService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
@RestController
public class RegisterController {

    private final RegisterService service;

    @PostMapping("/auth/register")
    public Mono<ResponseEntity<CreatedFullResponseDto>> registerUser(@Valid @RequestBody CreateFullRequestDto requestDto) {

        return service.registerUser(requestDto)
                .map(body -> ResponseEntity.status(HttpStatus.CREATED).body(body));
    }
}
