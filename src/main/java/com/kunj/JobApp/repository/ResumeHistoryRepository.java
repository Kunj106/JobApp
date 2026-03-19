package com.kunj.JobApp.repository;

import com.kunj.JobApp.entity.ResumeHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ResumeHistoryRepository extends JpaRepository<ResumeHistory,Long>
{
    List<ResumeHistory> findByUserIdOrderByCreatedAtDesc(Long userId);
}
