DROP TABLE IF EXISTS question_options;
DROP TABLE IF EXISTS questions;
DROP TABLE IF EXISTS exams;

CREATE TABLE exams (
                       id INT PRIMARY KEY AUTO_INCREMENT,
                       name VARCHAR(255) NOT NULL
);

CREATE TABLE questions (
                           id INT PRIMARY KEY AUTO_INCREMENT,
                           exam_id INT NOT NULL,
                           question_text VARCHAR(500) NOT NULL,
                           question_type ENUM('MULTIPLE_CHOICE','CLASSIC') NOT NULL,
                           correct_answer VARCHAR(500) NOT NULL,
                           FOREIGN KEY (exam_id) REFERENCES exams(id)
);

CREATE TABLE question_options (
                         id INT PRIMARY KEY AUTO_INCREMENT,
                         question_id INT NOT NULL,
                         option_text VARCHAR(255) NOT NULL,
                         is_correct BOOLEAN NOT NULL,
                         FOREIGN KEY (question_id) REFERENCES questions(id)
);
