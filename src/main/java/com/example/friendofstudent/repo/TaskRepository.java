package com.example.friendofstudent.repo;

import com.example.friendofstudent.model.Subject;
import com.example.friendofstudent.model.Task;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface TaskRepository extends CrudRepository<Task, Integer> {

    Task findTaskBySubjectAndNumber(Subject subject, int number);

    Task findTaskBySubjectAndQuestion(Subject subject, String question);

    List<Task> findAllBySubject(Subject subject);

    void deleteAllBySubject(Subject subject);

}
