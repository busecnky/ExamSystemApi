package main.java.com.examsystem.dto;

public record AnswerDto(int questionId,
                        int userId,
                        Integer optionId,
                        String textAnswer) {
}

