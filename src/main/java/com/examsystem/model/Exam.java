package main.java.com.examsystem.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class Exam {
    private int id;
    private String name;
    private List<Question> questions;

    public Exam() {
    }

    public Exam(int id, String name, List<Question> questions) {
        this.id = id;
        this.name = name;
        this.questions = questions;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Question> getQuestions() {
        return questions;
    }

    public void setQuestions(List<Question> questions) {
        this.questions = questions;
    }

}
