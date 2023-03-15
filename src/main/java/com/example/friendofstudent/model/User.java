package com.example.friendofstudent.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

@Entity(name = "user_data")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {
    @Id
    private Long chatId;
    private String firstName;
    private String lastName;
    private String username;
    private Timestamp registeredAt;
}
