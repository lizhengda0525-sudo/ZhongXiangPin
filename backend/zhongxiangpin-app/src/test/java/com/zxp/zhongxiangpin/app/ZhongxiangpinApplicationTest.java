package com.zxp.zhongxiangpin.app;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {
        "spring.profiles.active=test",
        "spring.datasource.hikari.initialization-fail-timeout=-1"
})
class ZhongxiangpinApplicationTest {

    @LocalServerPort
    private int port;

    private final TestRestTemplate restTemplate = new TestRestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void healthEndpointRespondsWithUnifiedBody() throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.add("X-Trace-Id", "trace-health-integration");
        ResponseEntity<String> response = restTemplate.exchange(
                "http://127.0.0.1:" + port + "/api/v1/health",
                org.springframework.http.HttpMethod.GET,
                new HttpEntity<>(headers),
                String.class
        );
        JsonNode body = objectMapper.readTree(response.getBody());

        assertEquals(200, response.getStatusCode().value());
        assertEquals("trace-health-integration", response.getHeaders().getFirst("X-Trace-Id"));
        assertEquals("0000", body.path("code").asText());
        assertEquals("成功", body.path("message").asText());
        assertEquals("UP", body.path("data").path("app").asText());
        assertNotNull(body.path("data").path("db").asText(null));
        assertNotNull(body.path("data").path("redis").asText(null));
        assertEquals("test", body.path("data").path("profile").asText());
    }
}
