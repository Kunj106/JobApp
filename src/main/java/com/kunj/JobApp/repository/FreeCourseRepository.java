package com.kunj.JobApp.repository;

import com.kunj.JobApp.entity.CourseCategory;
import com.kunj.JobApp.entity.FreeCourse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface FreeCourseRepository extends JpaRepository<FreeCourse,Long>
{
    List<FreeCourse> findByCategory(CourseCategory category);

    List<FreeCourse> findByTechnologyIgnoreCase(String technology);

    @Query("SELECT c FROM FreeCourse c WHERE " +
            "LOWER(c.title) LIKE %:query% OR " +
            "LOWER(c.technology) LIKE %:query% OR " +
            "LOWER(c.platform) LIKE %:query%")
    List<FreeCourse> searchCourses(@Param("query") String query);

    List<FreeCourse> findByPlatformIgnoreCase(String platform);

    // Newest first — auto-fetched courses float to top
    List<FreeCourse> findAllByOrderByFetchedAtDescTechnologyAsc();

    // Deduplication check by YouTube video/playlist ID
    boolean existsBySourceId(String sourceId);
    Optional<FreeCourse> findBySourceId(String sourceId);

    // Only auto-fetched courses
    List<FreeCourse> findByIsAutoFetchedTrueOrderByFetchedAtDesc();

    // Courses added in last N hours (for "NEW" badge)
    @Query("SELECT c FROM FreeCourse c WHERE c.fetchedAt >= :since ORDER BY c.fetchedAt DESC")
    List<FreeCourse> findRecentlyAdded(@Param("since") LocalDateTime since);

    long countByIsAutoFetchedTrue();
}

