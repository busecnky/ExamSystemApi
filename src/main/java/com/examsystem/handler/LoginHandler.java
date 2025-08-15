package main.java.com.examsystem.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import main.java.com.examsystem.service.AuthService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;

public class LoginHandler implements HttpHandler {
    private static final Logger logger = LoggerFactory.getLogger(LoginHandler.class);
    private final AuthService authService = new AuthService();

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if (!"POST".equals(exchange.getRequestMethod())) {
            logger.warn("Invalid request method: {}", exchange.getRequestMethod());
            exchange.sendResponseHeaders(405, -1);
            return;
        }

        ObjectMapper objectMapper = new ObjectMapper();

        try {
            Map<String, String> body = objectMapper.readValue(exchange.getRequestBody(), Map.class);
            String username = body.get("username");
            String password = body.get("password");

            logger.info("Login attempt for user: {}", username);

            authService.authenticate(username, password).ifPresentOrElse(user -> {
                try {
                    String token = authService.generateToken(user);
                    String response = objectMapper.writeValueAsString(Map.of("token", token));

                    exchange.getResponseHeaders().add("Content-Type", "application/json");
                    exchange.sendResponseHeaders(200, response.getBytes().length);

                    try (OutputStream os = exchange.getResponseBody()) {
                        os.write(response.getBytes());
                    }

                    logger.info("Login successful for user: {}", username);
                } catch (IOException e) {
                    logger.error("Error while responding to successful login", e);
                }
            }, () -> {
                try {
                    String errorResponse = objectMapper.writeValueAsString(Map.of("error", "Invalid username or password"));
                    exchange.getResponseHeaders().add("Content-Type", "application/json");
                    exchange.sendResponseHeaders(401, errorResponse.getBytes().length);

                    try (OutputStream os = exchange.getResponseBody()) {
                        os.write(errorResponse.getBytes());
                    }

                    logger.warn("Login failed for user: {}", username);
                } catch (IOException e) {
                    logger.error("Error while responding to failed login", e);
                }
            });
        } catch (IOException e) {
            logger.error("Failed to parse request body", e);
            String errorResponse = objectMapper.writeValueAsString(Map.of("error", "Invalid request payload"));
            exchange.getResponseHeaders().add("Content-Type", "application/json");
            exchange.sendResponseHeaders(400, errorResponse.getBytes().length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(errorResponse.getBytes());
            }
        }
    }
}
