package com.example.friendofstudent.model;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class AddSubjectProcess {
    private long chatId;
    private boolean isCourseSaved;
    private boolean isProfessorSaved;
    private boolean isSubjectNameSaved;
    private Subject subject = new Subject();

    public AddSubjectProcess(long chatId) {
        this.chatId = chatId;
    }
}
