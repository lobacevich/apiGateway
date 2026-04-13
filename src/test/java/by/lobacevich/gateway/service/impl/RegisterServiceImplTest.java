package by.lobacevich.gateway.service.impl;

import by.lobacevich.gateway.client.AuthClient;
import by.lobacevich.gateway.client.UserClient;
import by.lobacevich.gateway.dto.AuthRegisterRequestDto;
import by.lobacevich.gateway.dto.AuthRegisteredResponseDto;
import by.lobacevich.gateway.dto.CreateFullRequestDto;
import by.lobacevich.gateway.dto.CreatedFullResponseDto;
import by.lobacevich.gateway.dto.UserCreateRequestDto;
import by.lobacevich.gateway.dto.UserCreatedResponseDto;
import by.lobacevich.gateway.exception.ServiceException;
import by.lobacevich.gateway.exception.ServiceUnavailableException;
import by.lobacevich.gateway.mapper.RegisterMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import reactor.core.publisher.Mono;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RegisterServiceImplTest {

    private static final Long ID = 1L;
    private static final String ERROR = "Error";

    @Mock
    private AuthClient authClient;

    @Mock
    private UserClient userClient;

    @Mock
    private RegisterMapper mapper;

    @Mock
    private CreateFullRequestDto fullRequestDto;

    @Mock
    private UserCreateRequestDto createRequestDto;

    @Mock
    private AuthRegisterRequestDto registerRequestDto;

    @Mock
    private UserCreatedResponseDto createdResponseDto;

    @Mock
    private AuthRegisteredResponseDto registeredResponseDto;

    @Mock
    private CreatedFullResponseDto fullResponseDto;

    @InjectMocks
    private RegisterServiceImpl service;

    @Test
    void registerUser_ShouldCreateAndRegisterUserAndReturnMonoOfCreatedFullResponseDto() {
        when(mapper.toUserCreateRequest(fullRequestDto)).thenReturn(createRequestDto);
        when(userClient.create(createRequestDto)).thenReturn(Mono.just(createdResponseDto));
        when(createdResponseDto.id()).thenReturn(ID);
        when(mapper.toAuthRegisterRequest(fullRequestDto, ID)).thenReturn(registerRequestDto);
        when(authClient.register(registerRequestDto)).thenReturn(Mono.just(registeredResponseDto));
        when(mapper.toResponseFull(createdResponseDto, registeredResponseDto)).thenReturn(fullResponseDto);

        CreatedFullResponseDto actual = service.registerUser(fullRequestDto).block();

        verify(userClient, times(1)).create(createRequestDto);
        verify(authClient, times(1)).register(registerRequestDto);
        verify(userClient, never()).delete(any());
        assertEquals(fullResponseDto, actual);
    }

    @Test
    void registerUser_ShouldCreateAndThenDeleteUserAndThrowServiceException() {
        when(mapper.toUserCreateRequest(fullRequestDto)).thenReturn(createRequestDto);
        when(userClient.create(createRequestDto)).thenReturn(Mono.just(createdResponseDto));
        when(createdResponseDto.id()).thenReturn(ID);
        when(mapper.toAuthRegisterRequest(fullRequestDto, ID)).thenReturn(registerRequestDto);
        when(authClient.register(registerRequestDto))
                .thenReturn(Mono.error(new ServiceException(ERROR, HttpStatus.BAD_REQUEST)));
        when(userClient.delete(ID)).thenReturn(Mono.empty());

        Mono<CreatedFullResponseDto> actual = service.registerUser(fullRequestDto);

        assertThrows(ServiceException.class, actual::block);
        verify(userClient, times(1)).create(createRequestDto);
        verify(authClient, times(1)).register(registerRequestDto);
        verify(userClient, times(1)).delete(ID);
        verify(mapper, never()).toResponseFull(any(), any());
    }

    @Test
    void registerUser_ShouldCreateAndNotDeleteUserAndThrowServiceException() {
        when(mapper.toUserCreateRequest(fullRequestDto)).thenReturn(createRequestDto);
        when(userClient.create(createRequestDto)).thenReturn(Mono.just(createdResponseDto));
        when(createdResponseDto.id()).thenReturn(ID);
        when(mapper.toAuthRegisterRequest(fullRequestDto, ID)).thenReturn(registerRequestDto);
        when(authClient.register(registerRequestDto))
                .thenReturn(Mono.error(new ServiceException(ERROR, HttpStatus.BAD_REQUEST)));
        when(userClient.delete(ID))
                .thenReturn(Mono.error(new ServiceUnavailableException(ERROR)));

        Mono<CreatedFullResponseDto> actual = service.registerUser(fullRequestDto);

        assertThrows(ServiceException.class, actual::block);
        verify(userClient, times(1)).create(createRequestDto);
        verify(authClient, times(1)).register(registerRequestDto);
        verify(userClient, times(1)).delete(ID);
        verify(mapper, never()).toResponseFull(any(), any());
    }

    @Test
    void registerUser_ShouldThrowServiceUnavailableExceptionOnUserCreation() {
        when(mapper.toUserCreateRequest(fullRequestDto)).thenReturn(createRequestDto);
        when(userClient.create(createRequestDto))
                .thenReturn(Mono.error(new ServiceUnavailableException(ERROR)));

        Mono<CreatedFullResponseDto> actual = service.registerUser(fullRequestDto);

        assertThrows(ServiceUnavailableException.class, actual::block);
        verify(userClient, times(1)).create(createRequestDto);
        verify(authClient, never()).register(any());
        verify(userClient, never()).delete(any());
    }
}