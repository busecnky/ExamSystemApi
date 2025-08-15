package main.java.com.examsystem.repository.impl;

import main.java.com.examsystem.config.DatabaseConfig;
import main.java.com.examsystem.repository.OptionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class OptionRepositoryImpl implements OptionRepository {
    private static final Logger logger = LoggerFactory.getLogger(OptionRepositoryImpl.class);

    @Override
    public List<String> findByQuestionId(int questionId) {
        List<String> options = new ArrayList<>();
        String sql = "SELECT option_text FROM question_options WHERE question_id = ?";

        try (Connection connection = DatabaseConfig.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setInt(1, questionId);
            try (ResultSet resultSet = ps.executeQuery()) {
                while (resultSet.next()) {
                    options.add(resultSet.getString("option_text"));
                }
            }
        } catch (Exception e) {
            logger.error("Error occurred while processing request", e);
        }
        return options;
    }
}
