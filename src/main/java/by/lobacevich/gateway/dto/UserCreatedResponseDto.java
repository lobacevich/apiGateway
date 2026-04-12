package by.lobacevich.gateway.dto;

public record UserCreatedResponseDto(Long id,
                                     String name,
                                     String surname,
                                     String birthDate,
                                     String email,
                                     Boolean active,
                                     String createdAt,
                                     String updatedAt) {
}
