package main.java.com.examsystem.strategy;

import main.java.com.examsystem.model.Question;

public class MultipleChoiceStrategy implements ScoreStrategy {
    @Override
    public int score(Question question, String userAnswer) {
        if (question.getOptions() == null || question.getCorrectAnswer() == null) return 0;

        try {
            int selectedIndex = Integer.parseInt(userAnswer);
            String selectedValue = question.getOptions().get(selectedIndex);
            return selectedValue.equalsIgnoreCase(question.getCorrectAnswer()) ? 1 : 0;
        } catch (Exception e) {
            return 0;
        }
    }
}
