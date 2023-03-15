package com.example.friendofstudent.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@Entity
public class Subject {
    @Id
    @GeneratedValue
    private int id;
    private int course;
    private String professor;
    private String subjectName;

    public String toString() {
        return subjectName + " " + professor + " " + course + " курс";
    }
}
