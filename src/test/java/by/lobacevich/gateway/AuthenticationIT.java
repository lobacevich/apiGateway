package by.lobacevich.gateway;

import com.github.tomakehurst.wiremock.client.WireMock;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;

@SpringBootTest
@AutoConfigureWebTestClient
@AutoConfigureWireMock(port = 0)
@TestPropertySource(properties = {
        "authClient.url=http://localhost:${wiremock.server.port}",
        "orderService.url=http://localhost:${wiremock.server.port}",
})
class AuthenticationIT {

    private static final String TOKEN_PREFIX = "Bearer ";
    private static final String TOKEN = "token";
    private static final String TOKEN_REQUEST = """
            {
               "token": "token"
            }
            """;
    private static final String TOKEN_INFO = """
            {
                "type": "ACCESS",
                "userId": 1,
                "role": "ROLE_USER"
            }
            """;
    private static final String ORDER = """
            {
                "id": 6,
                "userId": 1
            }
            """;

    @Autowired
    private WebTestClient webTestClient;

    @Test
    void authenticateAndSendRequest_ShouldValidateTokenAndSentToOrderWithHeaders() {
        stubFor(WireMock.post(urlPathEqualTo("/auth/validate"))
                .withRequestBody(equalToJson(TOKEN_REQUEST))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(TOKEN_INFO)));

        stubFor(WireMock.get(urlPathEqualTo("/orders/6"))
                .withHeader("X-User-Id", equalTo("1"))
                .withHeader("X-Role", equalTo("ROLE_USER"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(ORDER)));

        webTestClient.get()
                .uri("/orders/6")
                .header(HttpHeaders.AUTHORIZATION, TOKEN_PREFIX + TOKEN)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.id").isEqualTo(6)
                .jsonPath("$.userId").isEqualTo(1);

        verify(1, postRequestedFor(urlPathEqualTo("/auth/validate"))
                .withRequestBody(equalToJson(TOKEN_REQUEST)));
        verify(1, getRequestedFor(urlPathEqualTo("/orders/6"))
                .withHeader("X-User-Id", equalTo("1"))
                .withHeader("X-Role", equalTo("ROLE_USER")));
    }
}
