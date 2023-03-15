package com.example.friendofstudent.repo;

import com.example.friendofstudent.model.User;
import org.springframework.data.repository.CrudRepository;

public interface UserRepository extends CrudRepository<User, Long> {
}
