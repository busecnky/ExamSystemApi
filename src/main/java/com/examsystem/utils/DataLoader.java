package main.java.com.examsystem.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import main.java.com.examsystem.config.DatabaseConfig;
import main.java.com.examsystem.model.Exam;
import main.java.com.examsystem.model.Question;
import main.java.com.examsystem.model.enums.QuestionType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

public class DataLoader {

    private static final Logger logger = LoggerFactory.getLogger(DataLoader.class);

    public static List<Exam> loadExams() {
        logger.info("Starting to load exams from JSON file.");

        try {
            ObjectMapper mapper = new ObjectMapper();
            InputStream inputStream = DataLoader.class.getResourceAsStream("/data/exams.json");

            if (inputStream == null) {
                logger.error("exams.json file not found at /data/exams.json");
                throw new RuntimeException("exams.json file not found.");
            }

            logger.debug("Successfully found exams.json file.");

            List<Exam> exams = List.of(mapper.readValue(inputStream, Exam[].class));
            logger.info("Successfully loaded {} exams.", exams.size());

            return exams;

        } catch (Exception e) {
            logger.error("An error occurred while loading exam data.", e);
            throw new RuntimeException("Exam data could not be loaded", e);
        }
    }

    public static void saveExamsToDatabase() {
        logger.info("Starting to save exams to the database.");

        List<Exam> exams = loadExams();

        String examSql = "INSERT INTO exams (id, name) VALUES (?, ?)";
        String questionSql = "INSERT INTO questions (id, exam_id, question_text, question_type, correct_answer) VALUES (?, ?, ?, ?, ?)";
        String optionSql = "INSERT INTO question_options (question_id, option_text, is_correct) VALUES (?, ?, ?)";

        try (Connection connection = DatabaseConfig.getConnection();
             PreparedStatement examPreparedStatement = connection.prepareStatement(examSql);
             PreparedStatement questionPreparedStatement = connection.prepareStatement(questionSql);
             PreparedStatement optionPreparedStatement = connection.prepareStatement(optionSql)) {

            for (Exam exam : exams) {
                logger.debug("Saving exam with ID: {} and name: '{}'", exam.getId(), exam.getName());

                examPreparedStatement.setInt(1, exam.getId());
                examPreparedStatement.setString(2, exam.getName());
                examPreparedStatement.executeUpdate();

                for (Question question : exam.getQuestions()) {
                    logger.debug("Saving question with ID: {} for exam ID: {}", question.getId(), exam.getId());

                    questionPreparedStatement.setInt(1, question.getId());
                    questionPreparedStatement.setInt(2, exam.getId());
                    questionPreparedStatement.setString(3, question.getText());
                    questionPreparedStatement.setString(4, question.getType().name());
                    questionPreparedStatement.setString(5, question.getCorrectAnswer());
                    questionPreparedStatement.executeUpdate();

                    if (question.getType() == QuestionType.MULTIPLE_CHOICE && question.getOptions() != null) {
                        for (String optionText : question.getOptions()) {
                            logger.debug("Saving option '{}' for question ID: {}", optionText, question.getId());

                            optionPreparedStatement.setInt(1, question.getId());
                            optionPreparedStatement.setString(2, optionText);
                            optionPreparedStatement.setBoolean(3, optionText.equals(question.getCorrectAnswer()));
                            optionPreparedStatement.executeUpdate();
                        }
                    }
                }
            }
            logger.info("Successfully saved all exam data to the database. {} exams were processed.", exams.size());

        } catch (SQLException e) {
            logger.error("An SQL error occurred while saving data to the database.", e);
            throw new RuntimeException("An error occurred while saving data to the database ", e);
        }
    }
}