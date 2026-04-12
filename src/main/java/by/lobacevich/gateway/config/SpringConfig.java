package by.lobacevich.gateway.config;

import by.lobacevich.gateway.mapper.RegisterMapper;
import by.lobacevich.gateway.mapper.RegisterMapperImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SpringConfig {

    @Bean
    RegisterMapper registerMapper() {
        return new RegisterMapperImpl();
    }
}
