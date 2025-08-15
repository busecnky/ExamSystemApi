package main.java.com.examsystem.repository;

import main.java.com.examsystem.model.Exam;

import java.util.List;
import java.util.Optional;

public interface ExamRepository {
    List<Exam> findAll();
    Optional<Exam> findById(int id);
}
