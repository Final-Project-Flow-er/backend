package com.chaing.domain.notices.repository;

import com.chaing.domain.notices.entity.Notice;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface NoticeRepository extends JpaRepository<Notice, Long> {

    @Query("SELECT n FROM Notice n ORDER BY " +
            "(CASE WHEN n.important = true AND (n.importantUntil IS NULL OR n.importantUntil > :now) THEN 1 ELSE 0 END) DESC, " +
            "n.createdAt DESC")
    Page<Notice> findAllSorted(@Param("now") LocalDateTime now, Pageable pageable);

    @Query("SELECT n.noticeId FROM Notice n ORDER BY " +
            "(CASE WHEN n.important = true AND (n.importantUntil IS NULL OR n.importantUntil > :now) THEN 1 ELSE 0 END) DESC, " +
            "n.createdAt DESC")
    List<Long> findAllIdsSorted(@Param("now") LocalDateTime now);

    @Query("SELECT COUNT(n) FROM Notice n WHERE n.important = true AND (n.importantUntil IS NULL OR n.importantUntil > :now)")
    long countEffectiveImportantNotices(@Param("now") LocalDateTime now);

    @Query(value = "SELECT * FROM notice n WHERE n.deleted_at IS NULL AND " +
            "((CASE WHEN n.important = true AND (n.important_until IS NULL OR n.important_until > :now) " +
            "THEN 1 ELSE 0 END, n.created_at) > (:isImportant, :createdAt)) " +
            "ORDER BY (CASE WHEN n.important = true AND (n.important_until IS NULL OR n.important_until > :now) " +
            "THEN 1 ELSE 0 END) ASC, n.created_at ASC LIMIT 1",
            nativeQuery = true)
    Notice findPreviousNotice(@Param("now") LocalDateTime now, @Param("isImportant") int isImportant, @Param("createdAt") LocalDateTime createdAt);

    @Query(value = "SELECT * FROM notice n WHERE n.deleted_at IS NULL AND " +
            "((CASE WHEN n.important = true AND (n.important_until IS NULL OR n.important_until > :now) " +
            "THEN 1 ELSE 0 END, n.created_at) < (:isImportant, :createdAt)) " +
            "ORDER BY (CASE WHEN n.important = true AND (n.important_until IS NULL OR n.important_until > :now) " +
            "THEN 1 ELSE 0 END) DESC, n.created_at DESC LIMIT 1",
            nativeQuery = true)
    Notice findNextNotice(@Param("now") LocalDateTime now, @Param("isImportant") int isImportant, @Param("createdAt") LocalDateTime createdAt);
}
