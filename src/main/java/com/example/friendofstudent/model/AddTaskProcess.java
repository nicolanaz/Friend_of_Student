package com.example.friendofstudent.model;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class AddTaskProcess {
    private long chatId;
    private boolean isSubjectSaved;
    private boolean isNumberSaved;
    private boolean isQuestionSaved;
    private boolean isAnswerSaved;
    private Task task = new Task();

    public AddTaskProcess(long chatId) {
        this.chatId = chatId;
    }
}
