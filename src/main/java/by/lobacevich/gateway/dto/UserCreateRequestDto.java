package by.lobacevich.gateway.dto;

public record UserCreateRequestDto(String name,
                                   String surname,
                                   String birthDate,
                                   String email) {
}
