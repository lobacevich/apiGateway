package by.lobacevich.gateway.mapper;

import by.lobacevich.gateway.dto.AuthRegisterRequestDto;
import by.lobacevich.gateway.dto.AuthRegisteredResponseDto;
import by.lobacevich.gateway.dto.CreateFullRequestDto;
import by.lobacevich.gateway.dto.CreatedFullResponseDto;
import by.lobacevich.gateway.dto.UserCreateRequestDto;
import by.lobacevich.gateway.dto.UserCreatedResponseDto;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface RegisterMapper {

    UserCreateRequestDto toUserCreateRequest(CreateFullRequestDto requestDto);

    AuthRegisterRequestDto toAuthRegisterRequest(CreateFullRequestDto requestDto, Long userId);

    CreatedFullResponseDto toResponseFull(UserCreatedResponseDto userResponseDto,
                                          AuthRegisteredResponseDto authResponseDto);
}
