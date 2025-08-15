package main.java.com.examsystem.strategy;

import main.java.com.examsystem.model.Question;

public class TextStrategy implements ScoreStrategy {
    @Override
    public int score(Question question, String userAnswer) {
        if (question.getCorrectAnswer() == null || userAnswer == null) return 0;

        String cleanUser = userAnswer.trim().toLowerCase();
        String cleanCorrect = question.getCorrectAnswer().trim().toLowerCase();

        return cleanUser.equals(cleanCorrect) ? 1 : 0;
    }
}
