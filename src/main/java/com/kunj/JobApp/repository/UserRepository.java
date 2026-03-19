package com.kunj.JobApp.repository;

import com.kunj.JobApp.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User,Long>
{
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
}
