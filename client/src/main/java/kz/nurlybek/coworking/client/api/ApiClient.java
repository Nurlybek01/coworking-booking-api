package kz.nurlybek.coworking.client.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import kz.nurlybek.coworking.client.dto.AuthResponse;
import kz.nurlybek.coworking.client.dto.LoginRequest;
import kz.nurlybek.coworking.client.session.SessionManager;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public class ApiClient {

    private static final String BASE_URL = "http://localhost:8080";
    private static final ApiClient INSTANCE = new ApiClient();

    private final HttpClient http;
    private final ObjectMapper mapper;

    private ApiClient() {
        this.http = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
        this.mapper = new ObjectMapper()
                .registerModule(new JavaTimeModule());
    }

    public static ApiClient get() {
        return INSTANCE;
    }

    public AuthResponse login(String email, String password) {
        try {
            String body = mapper.writeValueAsString(new LoginRequest(email, password));
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/api/auth/login"))
                    .timeout(Duration.ofSeconds(15))
                    .header("Content-Type", "application/json")
                    .header("Accept", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build();

            HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());

            if (resp.statusCode() != 200) {
                throw new ApiException(resp.statusCode(), extractError(resp.body()));
            }
            return mapper.readValue(resp.body(), AuthResponse.class);
        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            throw new ApiException(0, "Сервер недоступен: " + e.getMessage());
        }
    }

    public JsonNode get(String path) {
        try {
            HttpRequest.Builder b = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + path))
                    .timeout(Duration.ofSeconds(15))
                    .header("Accept", "application/json")
                    .GET();
            addAuth(b);

            HttpResponse<String> resp = http.send(b.build(), HttpResponse.BodyHandlers.ofString());
            if (resp.statusCode() >= 400) {
                throw new ApiException(resp.statusCode(), extractError(resp.body()));
            }
            return mapper.readTree(resp.body());
        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            throw new ApiException(0, "Ошибка запроса: " + e.getMessage());
        }
    }

    private void addAuth(HttpRequest.Builder b) {
        String token = SessionManager.get().getAccessToken();
        if (token != null) {
            b.header("Authorization", "Bearer " + token);
        }
    }

    private String extractError(String body) {
        try {
            JsonNode node = mapper.readTree(body);
            if (node.has("detail"))  return node.get("detail").asText();
            if (node.has("message")) return node.get("message").asText();
            if (node.has("title"))   return node.get("title").asText();
        } catch (Exception ignored) {}
        return body;
    }
}