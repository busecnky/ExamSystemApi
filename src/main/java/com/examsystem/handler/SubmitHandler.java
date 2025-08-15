package main.java.com.examsystem.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import main.java.com.examsystem.dto.AnswerDto;
import main.java.com.examsystem.model.Exam;
import main.java.com.examsystem.model.Question;
import main.java.com.examsystem.repository.ExamRepository;
import main.java.com.examsystem.repository.impl.ExamRepositoryImpl;
import main.java.com.examsystem.service.AuthService;
import main.java.com.examsystem.strategy.MultipleChoiceStrategy;
import main.java.com.examsystem.strategy.ScoreStrategy;
import main.java.com.examsystem.strategy.TextStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class SubmitHandler implements HttpHandler {
    private static final Logger logger = LoggerFactory.getLogger(SubmitHandler.class);

    private final ExamRepository examRepository = new ExamRepositoryImpl();
    private final AuthService authService = new AuthService();
    private final ObjectMapper objectMapper = new ObjectMapper();

    static class SubmitRequest {
        public int examId;
        public List<AnswerDto> answers;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        logger.info("SubmitHandler called: Method={}", exchange.getRequestMethod());

        if (!"POST".equals(exchange.getRequestMethod())) {
            logger.warn("Invalid HTTP method: {}", exchange.getRequestMethod());
            exchange.sendResponseHeaders(405, -1);
            return;
        }

        String authHeader = exchange.getRequestHeaders().getFirst("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            logger.warn("Authorization header missing or invalid");
            exchange.sendResponseHeaders(401, -1);
            return;
        }

        String token = authHeader.substring(7);
        if (authService.validateToken(token).isEmpty()) {
            logger.warn("Invalid token");
            exchange.sendResponseHeaders(401, -1);
            return;
        }

        try {
            SubmitRequest request = objectMapper.readValue(exchange.getRequestBody(), SubmitRequest.class);
            int examId = request.examId;
            List<AnswerDto> answers = request.answers;

            Optional<Exam> examOpt = examRepository.findById(examId);
            if (examOpt.isEmpty()) {
                logger.warn("Exam not found: {}", examId);
                exchange.sendResponseHeaders(404, -1);
                return;
            }

            Exam exam = examOpt.get();
            int correctCount = 0;
            int totalQuestions = exam.getQuestions().size();

            for (Question question : exam.getQuestions()) {
                Optional<AnswerDto> userAnswerOpt = answers.stream()
                        .filter(a -> a.questionId() == question.getId())
                        .findFirst();

                if (userAnswerOpt.isEmpty()) {
                    logger.info("No answer provided: questionId={}", question.getId());
                    continue;
                }
                AnswerDto userAnswer = userAnswerOpt.get();
                logger.info("Evaluating: questionId={}, userAnswer={}", question.getId(), userAnswer);

                ScoreStrategy strategy = switch (question.getType().name().toUpperCase()) {
                    case "MULTIPLE_CHOICE" -> new MultipleChoiceStrategy();
                    case "TEXT", "CLASSIC" -> new TextStrategy();
                    default -> null;
                };


                if (strategy == null) {
                    logger.warn("No strategy found for questionType={}", question.getType().name());
                    continue;
                }

                int score = 0;
                if (question.getType().name().equals("MULTIPLE_CHOICE") && userAnswer.optionId() != null) {
                    score = strategy.score(question, String.valueOf(userAnswer.optionId()));
                } else if (userAnswer.textAnswer() != null) {
                    score = strategy.score(question, userAnswer.textAnswer());
                }

                if (score > 0) correctCount++;
            }

            double percentageScore = totalQuestions == 0 ? 0 : ((double) correctCount / totalQuestions) * 100;

            String response = objectMapper.writeValueAsString(Map.of(
                    "score", percentageScore,
                    "correct", correctCount,
                    "total", totalQuestions
            ));

            exchange.getResponseHeaders().add("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, response.getBytes().length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(response.getBytes());
            }
            logger.info("Answers successfully submitted: examId={}", examId);

        } catch (Exception e) {
            logger.error("Error occurred while processing submission", e);
            exchange.sendResponseHeaders(500, -1);
        }
    }
}
