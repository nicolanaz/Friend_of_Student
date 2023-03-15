package com.example.friendofstudent.service;

import com.example.friendofstudent.model.Subject;
import com.example.friendofstudent.model.Task;
import com.example.friendofstudent.repo.SubjectRepository;
import com.example.friendofstudent.repo.TaskRepository;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class TaskService {
    private SubjectRepository subjectRepository;
    private TaskRepository taskRepository;

    public TaskService(SubjectRepository subjectRepository, TaskRepository taskRepository) {
        this.subjectRepository = subjectRepository;
        this.taskRepository = taskRepository;
    }

    public Iterable<Subject> getAllSubjects() {
        return subjectRepository.findAll();
    }

    public Subject saveSubject(Subject subject) {
        return subjectRepository.save(subject);
    }

    public Subject getSubject(int course, String professor, String subjectName) {
        return subjectRepository.findSubjectByCourseAndProfessorAndSubjectName(course, professor, subjectName);
    }

    public Optional<Subject> getSubjectById(int id) {
        return subjectRepository.findById(id);
    }

    public Task saveTask(Task task) {
        return taskRepository.save(task);
    }

    public List<Task> getAllTasksBySubject(Subject subject) {
        List<Task> allTasks = taskRepository.findAllBySubject(subject);

        Collections.sort(allTasks, new Comparator<Task>() {
            @Override
            public int compare(Task o1, Task o2) {
                return o1.getNumber() -  o2.getNumber();
            }
        });

        return allTasks;
    }

    public Task getTaskByNumber(Subject subject, int number) {
        return taskRepository.findTaskBySubjectAndNumber(subject, number);
    }

    public Task getTaskByQuestion(Subject subject, String question) {
        return taskRepository.findTaskBySubjectAndQuestion(subject, question);
    }
}
