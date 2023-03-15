package com.example.friendofstudent.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class Task {
    @Id
    @GeneratedValue
    private int id;

    @ManyToOne
    @JoinColumn(name = "subject_id")
    private Subject subject;

    private int number;

    @Column(columnDefinition = "TEXT")
    private String question;

    @Column(columnDefinition = "TEXT")
    private String answer;

    public String toString() {
        return "Вопрос: " + question + "\n\nОтвет: " + answer;
    }
}
