package com.chaing.api.security.principal;

import com.chaing.domain.users.entity.User;
import com.chaing.domain.users.enums.UserStatus;
import com.chaing.domain.users.exception.UserErrorCode;
import com.chaing.domain.users.exception.UserException;
import com.chaing.domain.users.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public UserDetails loadUserById(Long userId) throws UsernameNotFoundException {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserException(UserErrorCode.USER_NOT_FOUND));

        if (user.getStatus() == UserStatus.INACTIVE) {
            throw new UserException(UserErrorCode.INACTIVATED_USER);
        }

        if (user.isDeleted()) {
            throw new UserException(UserErrorCode.DELETED_USER);
        }

        return new UserPrincipal(user);
    }

    @Override
    public UserDetails loadUserByUsername(String loginId) throws UsernameNotFoundException {

        User user = userRepository.findByLoginId(loginId)
                .orElseThrow(() -> new UserException(UserErrorCode.USER_NOT_FOUND));

        if (user.getStatus() == UserStatus.INACTIVE) {
            throw new UserException(UserErrorCode.INACTIVATED_USER);
        }

        if (user.isDeleted()) {
            throw new UserException(UserErrorCode.DELETED_USER);
        }

        return new UserPrincipal(user);
    }
}
