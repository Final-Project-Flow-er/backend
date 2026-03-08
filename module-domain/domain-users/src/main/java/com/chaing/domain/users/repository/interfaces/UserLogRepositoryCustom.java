package com.chaing.domain.users.repository.interfaces;

import com.chaing.domain.users.dto.condition.UserLogSearchCondition;
import com.chaing.domain.users.entity.UserLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UserLogRepositoryCustom {

    Page<UserLog> searchUserLogs(UserLogSearchCondition condition, Pageable pageable);
}
