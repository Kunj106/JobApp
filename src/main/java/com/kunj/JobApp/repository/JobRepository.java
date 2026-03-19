package com.kunj.JobApp.repository;

import com.kunj.JobApp.entity.CompanyType;
import com.kunj.JobApp.entity.Job;
import com.kunj.JobApp.entity.JobType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface JobRepository extends JpaRepository<Job,Long>
{
    // Core search with all filters
    @Query("SELECT j FROM Job j WHERE j.isActive = true " +
            "AND (:companyType IS NULL OR j.companyType = :companyType) " +
            "AND (:jobType IS NULL OR j.jobType = :jobType) " +
            "AND (LOWER(j.title) LIKE %:keyword% OR LOWER(j.skills) LIKE %:keyword% OR LOWER(j.company) LIKE %:keyword%) " +
            "AND LOWER(j.location) LIKE %:location% " +
            "ORDER BY j.lastRefreshedAt DESC, j.postedAt DESC")
    List<Job> findWithFilters(
            @Param("companyType") CompanyType companyType,
            @Param("jobType") JobType jobType,
            @Param("keyword") String keyword,
            @Param("location") String location);

    // ── Time-filtered search ──────────────────────────────────────────────────
    @Query("SELECT j FROM Job j WHERE j.isActive = true " +
            "AND (:companyType IS NULL OR j.companyType = :companyType) " +
            "AND (:jobType IS NULL OR j.jobType = :jobType) " +
            "AND (LOWER(j.title) LIKE %:keyword% OR LOWER(j.skills) LIKE %:keyword% OR LOWER(j.company) LIKE %:keyword%) " +
            "AND LOWER(j.location) LIKE %:location% " +
            "AND j.lastRefreshedAt >= :since " +
            "ORDER BY j.lastRefreshedAt DESC")
    List<Job> findWithFiltersAndSince(
            @Param("companyType") CompanyType companyType,
            @Param("jobType") JobType jobType,
            @Param("keyword") String keyword,
            @Param("location") String location,
            @Param("since") LocalDateTime since);

    // ── Bulk refresh timestamps ───────────────────────────────────────────────
    @Modifying
    @Transactional
    @Query("UPDATE Job j SET j.lastRefreshedAt = :now WHERE j.isActive = true " +
            "AND (LOWER(j.title) LIKE %:keyword% OR LOWER(j.skills) LIKE %:keyword%) " +
            "AND LOWER(j.location) LIKE %:location%")
    int bulkRefreshTimestamp(
            @Param("keyword") String keyword,
            @Param("location") String location,
            @Param("now") LocalDateTime now);

    @Query("SELECT j.id FROM Job j WHERE j.isActive = true " +
            "AND (LOWER(j.title) LIKE %:keyword% OR LOWER(j.skills) LIKE %:keyword% OR LOWER(j.company) LIKE %:keyword%) " +
            "AND LOWER(j.location) LIKE %:location%")
    List<Long> findIdsByKeywordAndLocation(
            @Param("keyword") String keyword,
            @Param("location") String location);

    @Modifying
    @Transactional
    @Query("UPDATE Job j SET j.lastRefreshedAt = :now WHERE j.id IN :ids")
    int refreshByIds(@Param("ids") List<Long> ids, @Param("now") LocalDateTime now);

    List<Job> findByIsActiveTrueOrderByLastRefreshedAtDescPostedAtDesc();

    // ── Live-fetch deduplication ──────────────────────────────────────────────
    boolean existsBySourceId(String sourceId);
    Optional<Job> findBySourceId(String sourceId);

    // ── Live job queries ──────────────────────────────────────────────────────
    List<Job> findByIsLiveTrueAndIsActiveTrueOrderByPostedAtDesc();

    @Query("SELECT j FROM Job j WHERE j.isLive = true AND j.isActive = true " +
            "AND j.postedAt >= :since ORDER BY j.postedAt DESC")
    List<Job> findLiveJobsSince(@Param("since") LocalDateTime since);

    // ── Stats ─────────────────────────────────────────────────────────────────
    long countByIsLiveTrue();
    long countByIsLiveTrueAndPostedAtAfter(LocalDateTime since);
    long countBySourcePlatform(String platform);
}
