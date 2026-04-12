package by.lobacevich.gateway.dto;

public record AuthRegisterRequestDto(String login,
                                     String password,
                                     Long userId) {
}
