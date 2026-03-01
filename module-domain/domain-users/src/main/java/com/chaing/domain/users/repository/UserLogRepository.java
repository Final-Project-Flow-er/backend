package com.chaing.domain.users.repository;

import com.chaing.domain.users.entity.UserLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserLogRepository extends JpaRepository<UserLog, Long> {

    Page<UserLog> findAllByOrderByCreatedAtDesc(Pageable pageable);
}
