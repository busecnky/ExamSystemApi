package main.java.com.examsystem.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import main.java.com.examsystem.model.Exam;
import main.java.com.examsystem.repository.ExamRepository;
import main.java.com.examsystem.repository.impl.ExamRepositoryImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

public class ExamHandler implements HttpHandler {
    private static final Logger logger = LoggerFactory.getLogger(ExamHandler.class);
    private final ExamRepository examRepository = new ExamRepositoryImpl();

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try (exchange) {
            ObjectMapper objectMapper = new ObjectMapper();
            if ("GET".equals(exchange.getRequestMethod())) {
                String path = exchange.getRequestURI().getPath();
                String[] parts = path.split("/");

                if (parts.length == 2 && parts[1].equals("exams")) {
                    logger.info("Fetching all exams");

                    List<Exam> exams = examRepository.findAll();
                    String jsonResponse = objectMapper.writeValueAsString(exams);
                    exchange.getResponseHeaders().set("Content-Type", "application/json");
                    exchange.sendResponseHeaders(200, jsonResponse.getBytes().length);
                    exchange.getResponseBody().write(jsonResponse.getBytes());

                    logger.info("All exams successfully returned, count={}", exams.size());

                    return;
                }

                if (parts.length == 3) {
                    try {
                        int examId = Integer.parseInt(parts[2]);
                        logger.info("Fetching exam by id={}", examId);

                        Optional<Exam> optionalExam = examRepository.findById(examId);

                        if (optionalExam.isPresent()) {
                            Exam exam = optionalExam.get();
                            String jsonResponse = objectMapper.writeValueAsString(exam);
                            exchange.getResponseHeaders().set("Content-Type", "application/json");
                            exchange.sendResponseHeaders(200, jsonResponse.getBytes().length);
                            exchange.getResponseBody().write(jsonResponse.getBytes());
                        } else {
                            logger.warn("Exam not found: id={}", examId);
                            exchange.sendResponseHeaders(404, -1);
                        }
                    } catch (NumberFormatException e) {
                        logger.warn("Invalid exam ID format in request");
                        exchange.sendResponseHeaders(400, -1);
                    }
                } else {
                    logger.warn("Invalid URL format");
                    exchange.sendResponseHeaders(400, -1);
                }
            } else {
                logger.warn("Invalid HTTP method: {}", exchange.getRequestMethod());
                exchange.sendResponseHeaders(405, -1);
            }
        }catch (Exception e) {
            logger.error("Error occurred while processing request", e);
            exchange.sendResponseHeaders(500, -1);
        }
    }
}