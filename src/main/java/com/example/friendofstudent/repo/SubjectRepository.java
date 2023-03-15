package com.example.friendofstudent.repo;

import com.example.friendofstudent.model.Subject;
import org.springframework.data.repository.CrudRepository;

public interface SubjectRepository extends CrudRepository<Subject, Integer> {
    Subject findSubjectByCourseAndProfessorAndSubjectName(int course, String professor, String subjectName);

}
