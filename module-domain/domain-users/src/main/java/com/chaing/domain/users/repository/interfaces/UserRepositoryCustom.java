package com.chaing.domain.users.repository.interfaces;

import com.chaing.domain.users.dto.condition.UserSearchCondition;
import com.chaing.domain.users.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UserRepositoryCustom {

    Page<User> searchUsers(UserSearchCondition condition, Pageable pageable);
}
