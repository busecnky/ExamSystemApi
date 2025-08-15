package main.java.com.examsystem.model;

import main.java.com.examsystem.model.enums.QuestionType;

import java.util.List;

public class Question {
    private int id;
    private String text;
    private QuestionType type;
    private List<String> options;
    private String correctAnswer;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public QuestionType getType() {
        return type;
    }

    public void setType(QuestionType type) {
        this.type = type;
    }

    public List<String> getOptions() {
        return options;
    }

    public void setOptions(List<String> options) {
        this.options = options;
    }

    public String getCorrectAnswer() {
        return correctAnswer;
    }

    public void setCorrectAnswer(String correctAnswer) {
        this.correctAnswer = correctAnswer;
    }

    public Question(int id, String text, QuestionType type, List<String> options, String correctAnswer) {
        this.id = id;
        this.text = text;
        this.type = type;
        this.options = options;
        this.correctAnswer = correctAnswer;
    }
    public Question() {
    }
}
