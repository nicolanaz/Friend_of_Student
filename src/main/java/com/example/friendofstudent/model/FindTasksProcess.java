package com.example.friendofstudent.model;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class FindTasksProcess {
    private long chatId;
    private Subject subject;
    private boolean isSubjectSelected;

    public FindTasksProcess(long chatId) {
        this.chatId = chatId;
    }
}
