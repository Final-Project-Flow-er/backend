package com.chaing.domain.notices.repository;

import com.chaing.domain.notices.entity.Notice;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NoticeRepository extends JpaRepository<Notice,Long> {

    Page<Notice> findAllByOrderByImportantDescCreatedAtDesc(Pageable pageable);
}
