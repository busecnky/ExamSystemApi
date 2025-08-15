package main.java.com.examsystem.utils;

import main.java.com.examsystem.config.DatabaseConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.Statement;
import java.util.stream.Collectors;


public class DatabaseInitializer {
    private static final Logger logger = LoggerFactory.getLogger(DatabaseInitializer.class);

    public static void initializeDatabase() {
        String sqlScript;
        try (InputStream inputStream = DatabaseInitializer.class.getResourceAsStream("/schema.sql")) {
            assert inputStream != null;
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {


                sqlScript = reader.lines().collect(Collectors.joining("\n"));

            }
        } catch (Exception e) {
            logger.error("An error occurred while reading the SQL file.", e);
            return;
        }

        try (Connection connection = DatabaseConfig.getConnection();
             Statement statement = connection.createStatement()) {

            logger.debug("Executing SQL script to initialize database.");
            statement.execute(sqlScript);

            logger.info("Database schema successfully initialized.");

        } catch (Exception e) {
            logger.error("An error occurred while initializing the database.", e);
        }
    }
}
