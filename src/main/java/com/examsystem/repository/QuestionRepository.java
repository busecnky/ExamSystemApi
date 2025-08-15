package main.java.com.examsystem.repository;

import main.java.com.examsystem.model.Question;

import java.util.List;

public interface QuestionRepository {
    List<Question> findByExamId(int examId);
}
