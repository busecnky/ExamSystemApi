package main.java.com.examsystem.repository;

import java.util.List;

public interface OptionRepository {
    List<String> findByQuestionId(int questionId);

}
