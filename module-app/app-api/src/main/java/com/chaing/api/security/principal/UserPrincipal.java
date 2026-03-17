package com.chaing.api.security.principal;

import com.chaing.domain.users.entity.User;
import com.chaing.domain.users.enums.UserRole;
import com.chaing.domain.users.enums.UserStatus;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

@Getter
public class UserPrincipal implements UserDetails {

    private final Long id;
    private final String loginId;
    private final String password;
    private final UserRole role;
    private final UserStatus status;
    private final Long businessUnitId;

    public UserPrincipal(User user) {
        this.id = user.getUserId();
        this.loginId = user.getLoginId();
        this.password = user.getPassword();
        this.role = user.getRole();
        this.status = user.getStatus();
        this.businessUnitId = user.getBusinessUnitId();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    @Override
    public String getPassword() { return password; }
    @Override
    public String getUsername() { return loginId; }
    @Override
    public boolean isAccountNonExpired() { return true; }
    @Override
    public boolean isAccountNonLocked() { return status == UserStatus.ACTIVE; }
    @Override
    public boolean isCredentialsNonExpired() { return true; }
    @Override
    public boolean isEnabled() { return status == UserStatus.ACTIVE; }
}
