package com.examsystem.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import main.java.com.examsystem.handler.SubmitHandler;
import main.java.com.examsystem.model.Exam;
import main.java.com.examsystem.model.Question;
import main.java.com.examsystem.model.User;
import main.java.com.examsystem.model.enums.QuestionType;
import main.java.com.examsystem.repository.ExamRepository;
import main.java.com.examsystem.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SubmitHandlerTest {

    @Mock
    private ExamRepository examRepository;

    @Mock
    private AuthService authService;

    @Mock
    private HttpExchange httpExchange;

    private SubmitHandler submitHandler;

    private Headers requestHeaders;
    private ByteArrayOutputStream responseBody;

    @BeforeEach
    void setUp() throws Exception {
        submitHandler = new SubmitHandler();
        setMock(submitHandler, "examRepository", examRepository);
        setMock(submitHandler, "authService", authService);

        requestHeaders = new Headers();
        responseBody = new ByteArrayOutputStream();

    }
    private void setMock(Object target, String fieldName, Object mockObject) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, mockObject);
    }

    // Happy Path Tests

    @Test
    void handle_validSubmission_returns200AndCorrectScore() throws IOException {
        requestHeaders.add("Authorization", "Bearer valid-token");
        String requestJson = "{\"examId\": 1," +
                " \"answers\": " +
                "[{\"questionId\": 1, " +
                "\"optionId\": \"1\"}, " +
                "{\"questionId\": 2, " +
                "\"textAnswer\": \"correct\"}]}";
        InputStream requestBody = new ByteArrayInputStream(requestJson.getBytes());

        when(httpExchange.getResponseHeaders()).thenReturn(new Headers());
        when(httpExchange.getRequestHeaders()).thenReturn(requestHeaders);
        when(httpExchange.getResponseBody()).thenReturn(responseBody);

        when(httpExchange.getRequestMethod()).thenReturn("POST");
        when(httpExchange.getRequestBody()).thenReturn(requestBody);
        when(authService.validateToken("valid-token")).thenReturn(Optional.of(new User(1,"user1","1111")));

        Exam mockExam = new Exam();
        mockExam.setId(1);
        List<String> options = new ArrayList<>();
        options.add("A");
        options.add("B");
        options.add("C");

        Question q1 = new Question(1, "QuestionText", QuestionType.MULTIPLE_CHOICE, options, "B");
        Question q2 = new Question(2, "QuestionText", QuestionType.CLASSIC, null, "correct");
        mockExam.setQuestions(List.of(q1, q2));
        when(examRepository.findById(1)).thenReturn(Optional.of(mockExam));

        submitHandler.handle(httpExchange);

        verify(httpExchange).sendResponseHeaders(eq(200), anyLong());
        String response = responseBody.toString();
        Map<String, Object> responseMap = new ObjectMapper().readValue(response, Map.class);
        assertEquals(100.0, responseMap.get("score"));
        assertEquals(2, responseMap.get("correct"));
        assertEquals(2, responseMap.get("total"));
    }

    @Test
    void handle_partialCorrectSubmission_returnsOneCorrectScore() throws IOException {
        requestHeaders.add("Authorization", "Bearer valid-token");
        String requestJson = "{\"examId\": 1, " +
                "\"answers\": " +
                "[{\"questionId\": 1, " +
                "\"optionId\": 2}, " +
                "{\"questionId\": 2, " +
                "\"textAnswer\": \"correct\"}]}";
        InputStream requestBody = new ByteArrayInputStream(requestJson.getBytes());

        when(httpExchange.getResponseHeaders()).thenReturn(new Headers());
        when(httpExchange.getRequestHeaders()).thenReturn(requestHeaders);
        when(httpExchange.getResponseBody()).thenReturn(responseBody);

        when(httpExchange.getRequestMethod()).thenReturn("POST");
        when(httpExchange.getRequestBody()).thenReturn(requestBody);
        when(authService.validateToken("valid-token")).thenReturn(Optional.of(new User(1,"user1","1111")));

        Exam mockExam = new Exam();
        mockExam.setId(1);
        List<String> options = new ArrayList<>();
        options.add("A");
        options.add("B");
        options.add("C");
        Question q1 = new Question(1, "QuestionText", QuestionType.MULTIPLE_CHOICE, options, "A");
        Question q2 = new Question(2, "QuestionText", QuestionType.CLASSIC, null, "correct");
        mockExam.setQuestions(List.of(q1, q2));
        when(examRepository.findById(1)).thenReturn(Optional.of(mockExam));

        submitHandler.handle(httpExchange);

        verify(httpExchange).sendResponseHeaders(eq(200), anyLong());
        String response = responseBody.toString();
        Map<String, Object> responseMap = new ObjectMapper().readValue(response, Map.class);
        assertEquals(50.0, responseMap.get("score"));
        assertEquals(1, responseMap.get("correct"));
        assertEquals(2, responseMap.get("total"));
    }

    // Unhappy Path Tests
    @Test
    void handle_invalidMethod_returns405() throws IOException {
        when(httpExchange.getRequestMethod()).thenReturn("GET");

        submitHandler.handle(httpExchange);

        verify(httpExchange).sendResponseHeaders(405, -1);
    }

    @Test
    void handle_missingAuthorizationHeader_returns401() throws IOException {
        when(httpExchange.getRequestMethod()).thenReturn("POST");
        when(httpExchange.getRequestHeaders()).thenReturn(requestHeaders);

        submitHandler.handle(httpExchange);

        verify(httpExchange).sendResponseHeaders(401, -1);
    }

    @Test
    void handle_invalidToken_returns401() throws IOException {
        when(httpExchange.getRequestMethod()).thenReturn("POST");
        requestHeaders.add("Authorization", "Bearer invalid-token");
        when(authService.validateToken("invalid-token")).thenReturn(Optional.empty());
        when(httpExchange.getRequestHeaders()).thenReturn(requestHeaders);

        submitHandler.handle(httpExchange);

        verify(httpExchange).sendResponseHeaders(401, -1);
    }

    @Test
    void handle_examNotFound_returns404() throws IOException {
        requestHeaders.add("Authorization", "Bearer valid-token");
        String requestJson = "{\"examId\": 999, \"answers\": []}";
        InputStream requestBody = new ByteArrayInputStream(requestJson.getBytes());

        when(httpExchange.getRequestMethod()).thenReturn("POST");
        when(httpExchange.getRequestBody()).thenReturn(requestBody);
        when(authService.validateToken("valid-token")).thenReturn(Optional.of(new User(1,"user1","1111")));
        when(examRepository.findById(999)).thenReturn(Optional.empty());

        when(httpExchange.getRequestHeaders()).thenReturn(requestHeaders);

        submitHandler.handle(httpExchange);

        verify(httpExchange).sendResponseHeaders(404, -1);
    }

    @Test
    void handle_malformedJson_returns500() throws IOException {
        requestHeaders.add("Authorization", "Bearer valid-token");
        String requestJson = "{ \"examId\": 1, \"answers\": [ }";
        InputStream requestBody = new ByteArrayInputStream(requestJson.getBytes());

        when(httpExchange.getRequestMethod()).thenReturn("POST");
        when(httpExchange.getRequestBody()).thenReturn(requestBody);
        when(authService.validateToken("valid-token")).thenReturn(Optional.of(new User(1,"user1","1111")));
        when(httpExchange.getRequestHeaders()).thenReturn(requestHeaders);

        submitHandler.handle(httpExchange);

        verify(httpExchange).sendResponseHeaders(500, -1);
    }

    @Test
    void handle_noAnswersProvided_returnsCorrectScore() throws IOException {
        requestHeaders.add("Authorization", "Bearer valid-token");
        String requestJson = "{\"examId\": 1, \"answers\": []}";
        InputStream requestBody = new ByteArrayInputStream(requestJson.getBytes());

        when(httpExchange.getRequestMethod()).thenReturn("POST");
        when(httpExchange.getRequestBody()).thenReturn(requestBody);
        when(authService.validateToken("valid-token")).thenReturn(Optional.of(new User(1,"user1","1111")));

        when(httpExchange.getResponseHeaders()).thenReturn(new Headers());
        when(httpExchange.getRequestHeaders()).thenReturn(requestHeaders);

        ByteArrayOutputStream responseBody = new ByteArrayOutputStream();
        when(httpExchange.getResponseBody()).thenReturn(responseBody);

        Exam mockExam = new Exam();
        mockExam.setId(1);
        mockExam.setQuestions(Collections.emptyList());
        when(examRepository.findById(1)).thenReturn(Optional.of(mockExam));

        submitHandler.handle(httpExchange);

        verify(httpExchange).sendResponseHeaders(eq(200), anyLong());
        String response = responseBody.toString();
        Map<String, Object> responseMap = new ObjectMapper().readValue(response, Map.class);
        assertEquals(0.0, responseMap.get("score"));
        assertEquals(0, responseMap.get("correct"));
        assertEquals(0, responseMap.get("total"));
    }
}