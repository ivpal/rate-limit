package com.github.ivpal;

import io.restassured.builder.RequestSpecBuilder;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.DockerComposeContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.io.File;

import static io.restassured.RestAssured.given;
import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.text.IsEmptyString.emptyString;

@Testcontainers
public class ApplicationTest {
    @Container
    private static final DockerComposeContainer<?> containers = new DockerComposeContainer<>(new File("docker-compose.test.yml"))
        .withExposedService("app", 8000)
        .waitingFor("app", Wait.defaultWaitStrategy());

    @Test
    public void rateLimitTest() {
        var spec = new RequestSpecBuilder()
            .addFilters(asList(new ResponseLoggingFilter(), new RequestLoggingFilter()))
            .setBaseUri("http://localhost:8000/")
            .addHeader("X-Auth-Token", "token_1")
            .build();

        given()
            .spec(spec)
            .get("/route")
            .then()
            .assertThat()
            .statusCode(200)
            .header("X-RateLimit-Remaining", "0")
            .header("X-RateLimit-Limit", "1");

        given()
            .spec(spec)
            .get("/route")
            .then()
            .assertThat()
            .statusCode(429)
            .header("X-RateLimit-Remaining", "0")
            .header("X-RateLimit-Limit", "1")
            .header("X-RateLimit-Retry-After", not(emptyString()));
    }
}
