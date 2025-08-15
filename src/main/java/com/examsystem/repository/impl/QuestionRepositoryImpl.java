package main.java.com.examsystem.repository.impl;

import main.java.com.examsystem.config.DatabaseConfig;
import main.java.com.examsystem.model.Question;
import main.java.com.examsystem.model.enums.QuestionType;
import main.java.com.examsystem.repository.OptionRepository;
import main.java.com.examsystem.repository.QuestionRepository;
import main.java.com.examsystem.utils.DatabaseInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class QuestionRepositoryImpl implements QuestionRepository {
    private final OptionRepository optionRepository = new OptionRepositoryImpl();
    private static final Logger logger = LoggerFactory.getLogger(DatabaseInitializer.class);

    @Override
    public List<Question> findByExamId(int examId) {
        List<Question> questions = new ArrayList<>();
        String sql = "SELECT id, question_text, question_type, correct_answer FROM questions WHERE exam_id = ?";

        try (Connection connection = DatabaseConfig.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setInt(1, examId);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    Question question = new Question();
                    question.setId(resultSet.getInt("id"));
                    question.setText(resultSet.getString("question_text"));
                    question.setType(QuestionType.valueOf(resultSet.getString("question_type")));
                    question.setCorrectAnswer(resultSet.getString("correct_answer"));

                    if (question.getType() == QuestionType.MULTIPLE_CHOICE) {
                        question.setOptions(optionRepository.findByQuestionId(question.getId()));
                    }
                    questions.add(question);
                }
            }
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
        return questions;
    }
}
