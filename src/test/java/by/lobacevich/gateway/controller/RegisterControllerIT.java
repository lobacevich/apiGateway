package by.lobacevich.gateway.controller;

import by.lobacevich.gateway.dto.CreateFullRequestDto;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.http.Fault;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.time.LocalDate;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.deleteRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.exactly;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;

@SpringBootTest
@AutoConfigureWebTestClient
@AutoConfigureWireMock(port = 0)
@TestPropertySource(properties = {
        "authClient.url=http://localhost:${wiremock.server.port}",
        "userClient.url=http://localhost:${wiremock.server.port}"
})
class RegisterControllerIT {

    private static final CreateFullRequestDto FULL_REQUEST_DTO = new CreateFullRequestDto(
            "login",
            "password",
            "John",
            "Doe",
            LocalDate.of(1990, 1, 1),
            "john@example.com");
    private static final String USER_RESPONSE = """
            {
                "id": 1,
                "name": "John",
                "surname": "Doe",
                "birthDate": "1990-01-01",
                "email": "john@example.com",
                "active": true,
                "createdAt": "2026-01-01T10:00:00",
                "updatedAt": "2026-01-01T10:00:00"
            }
            """;
    private static final String AUTH_RESPONSE = """
            {
                "userId": 1,
                "login": "login",
                "role": "ROLE_USER"
            }
            """;
    private static final String DUPLICATE_EMAIL = """
            {
                "message": "Duplicate email"
            }
            """;
    private static final String LOGIN_EXISTS = """
            {
                "message": "Login already exists"
            }
            """;

    @Autowired
    private WebTestClient webTestClient;

    @BeforeEach
    void resetWireMock() {
        WireMock.reset();
    }

    @Test
    void registerUser_ShouldReturnCreatedFullRequestDto() {
        stubFor(WireMock.post(urlPathEqualTo("/users"))
                .willReturn(aResponse()
                        .withStatus(201)
                        .withHeader("Content-Type", "application/json")
                        .withBody(USER_RESPONSE)));

        stubFor(WireMock.post(urlPathEqualTo("/auth/register"))
                .willReturn(aResponse()
                        .withStatus(201)
                        .withHeader("Content-Type", "application/json")
                        .withBody(AUTH_RESPONSE)));

        webTestClient.post()
                .uri("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(FULL_REQUEST_DTO)
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.id").isEqualTo(1)
                .jsonPath("$.login").isEqualTo("login")
                .jsonPath("$.name").isEqualTo("John");
    }

    @Test
    void registerUser_ShouldReturnErrorWhenUserServiceFails() {
        stubFor(WireMock.post(urlPathEqualTo("/users"))
                .willReturn(aResponse()
                        .withStatus(400)
                        .withHeader("Content-Type", "application/json")
                        .withBody(DUPLICATE_EMAIL)));

        stubFor(WireMock.post(urlPathEqualTo("/auth/register"))
                .willReturn(aResponse()
                        .withStatus(201)
                        .withHeader("Content-Type", "application/json")
                        .withBody(AUTH_RESPONSE)));

        webTestClient.post()
                .uri("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(FULL_REQUEST_DTO)
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.message").isEqualTo("Duplicate email");

        verify(exactly(0), postRequestedFor(urlPathEqualTo("/auth/register")));
    }

    @Test
    void registerUser_ShouldReturnErrorWhenAuthServiceFailsAndRollBackUserService() {
        stubFor(WireMock.post(urlPathEqualTo("/users"))
                .willReturn(aResponse()
                        .withStatus(201)
                        .withHeader("Content-Type", "application/json")
                        .withBody(USER_RESPONSE)));

        stubFor(WireMock.post(urlPathEqualTo("/auth/register"))
                .willReturn(aResponse()
                        .withStatus(400)
                        .withHeader("Content-Type", "application/json")
                        .withBody(LOGIN_EXISTS)));

        stubFor(WireMock.delete(urlPathEqualTo("/users/1"))
                .willReturn(aResponse()
                        .withStatus(204)));

        webTestClient.post()
                .uri("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(FULL_REQUEST_DTO)
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.message").isEqualTo("Login already exists");

        verify(exactly(1), deleteRequestedFor(urlPathEqualTo("/users/1")));
    }

    @Test
    void registerUser_ShouldReturnErrorWhenAuthServiceFailsAndFailDeleteUser() {
        stubFor(WireMock.post(urlPathEqualTo("/users"))
                .willReturn(aResponse()
                        .withStatus(201)
                        .withHeader("Content-Type", "application/json")
                        .withBody(USER_RESPONSE)));

        stubFor(WireMock.post(urlPathEqualTo("/auth/register"))
                .willReturn(aResponse()
                        .withStatus(400)
                        .withHeader("Content-Type", "application/json")
                        .withBody(LOGIN_EXISTS)));

        stubFor(WireMock.delete(urlPathEqualTo("/users/1"))
                .willReturn(aResponse()
                        .withFault(Fault.EMPTY_RESPONSE)));

        webTestClient.post()
                .uri("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(FULL_REQUEST_DTO)
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.message").isEqualTo("Login already exists");

        verify(exactly(1), deleteRequestedFor(urlPathEqualTo("/users/1")));
    }
}
