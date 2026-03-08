    package com.chaing.domain.users.repository;

    import com.chaing.domain.users.entity.User;
    import com.chaing.domain.users.enums.UserRole;
    import com.chaing.domain.users.repository.interfaces.UserRepositoryCustom;
    import jakarta.persistence.LockModeType;
    import org.springframework.data.jpa.repository.JpaRepository;
    import org.springframework.data.jpa.repository.Lock;
    import org.springframework.data.jpa.repository.Query;
    import org.springframework.data.repository.query.Param;
    import org.springframework.stereotype.Repository;

    import java.util.List;
    import java.util.Optional;

    @Repository
    public interface UserRepository extends JpaRepository<User, Long>, UserRepositoryCustom {

        Optional<User> findByLoginId(String loginId);

        @Lock(LockModeType.PESSIMISTIC_WRITE)
        @Query("SELECT MAX(u.employeeNumber) FROM User u WHERE u.role = :role")
        Optional<String> findMaxEmployeeNumberByRole(@Param("role") UserRole role);

        @Lock(LockModeType.PESSIMISTIC_WRITE)
        @Query("SELECT MAX(u.loginId) FROM User u WHERE u.loginId LIKE :pattern%")
        Optional<String> findMaxLoginIdByPattern(@Param("pattern") String pattern);

        boolean existsByEmail(String email);

        @Query("SELECT u.userId FROM User u WHERE u.status = com.chaing.domain.users.enums.UserStatus.ACTIVE")
        List<Long> getAllActiveUserIds();
    }
