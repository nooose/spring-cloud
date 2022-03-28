package com.example.userservice.jpa;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends CrudRepository<UsersEntity, Long> {

    UsersEntity findByUserId(String userId);
    UsersEntity findByEmail(String username);
}
