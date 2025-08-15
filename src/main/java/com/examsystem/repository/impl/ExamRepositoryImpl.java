package main.java.com.examsystem.repository.impl;

import main.java.com.examsystem.config.DatabaseConfig;
import main.java.com.examsystem.model.Exam;
import main.java.com.examsystem.repository.ExamRepository;
import main.java.com.examsystem.repository.QuestionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ExamRepositoryImpl implements ExamRepository {
    private static final Logger logger = LoggerFactory.getLogger(ExamRepositoryImpl.class);
    private final QuestionRepository questionRepository = new QuestionRepositoryImpl();

    @Override
    public List<Exam> findAll() {
        List<Exam> exams = new ArrayList<>();
        String sql = "SELECT id, name FROM exams";

        try (Connection connection = DatabaseConfig.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql);
             ResultSet resultSet = preparedStatement.executeQuery()) {

            while (resultSet.next()) {
                Exam exam = new Exam();
                exam.setId(resultSet.getInt("id"));
                exam.setName(resultSet.getString("name"));
                exams.add(exam);
            }
        } catch (Exception e) {
            logger.error("Error occurred while processing request", e);
        }
        return exams;
    }

    @Override
    public Optional<Exam> findById(int id) {
        String sql = "SELECT id, name FROM exams WHERE id = ?";
        Exam exam = null;

        try (Connection connection = DatabaseConfig.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setInt(1, id);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    exam = new Exam();
                    exam.setId(resultSet.getInt("id"));
                    exam.setName(resultSet.getString("name"));
                    exam.setQuestions(questionRepository.findByExamId(exam.getId()));
                }
            }
        } catch (Exception e) {
            logger.error("Error occurred while processing request", e);
        }
        return Optional.ofNullable(exam);
    }
}
