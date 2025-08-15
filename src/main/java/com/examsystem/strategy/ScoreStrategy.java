package main.java.com.examsystem.strategy;

import main.java.com.examsystem.model.Question;

public interface ScoreStrategy {
    int score(Question question, String answer);
}

