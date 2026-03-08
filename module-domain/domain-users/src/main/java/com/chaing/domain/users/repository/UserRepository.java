    package com.chaing.domain.users.repository;

    import com.chaing.domain.users.entity.User;
    import com.chaing.domain.users.enums.UserRole;
    import com.chaing.domain.users.repository.interfaces.UserRepositoryCustom;
    import org.springframework.data.jpa.repository.JpaRepository;
    import org.springframework.data.jpa.repository.Query;
    import org.springframework.data.repository.query.Param;
    import org.springframework.stereotype.Repository;

    import java.util.List;
    import java.util.Optional;

    @Repository
    public interface UserRepository extends JpaRepository<User, Long>, UserRepositoryCustom {

        Optional<User> findByLoginId(String loginId);

        @Query(value = "SELECT MAX(employee_number) FROM user WHERE role = :#{#role.name()} FOR UPDATE", nativeQuery = true)
        Optional<String> findMaxEmployeeNumberByRole(@Param("role") UserRole role);

        @Query(value = "SELECT MAX(login_id) FROM user WHERE login_id LIKE CONCAT(:pattern, '%') FOR UPDATE", nativeQuery = true)
        Optional<String> findMaxLoginIdByPattern(@Param("pattern") String pattern);

        boolean existsByEmail(String email);

        @Query("SELECT u.userId FROM User u WHERE u.status = com.chaing.domain.users.enums.UserStatus.ACTIVE")
        List<Long> getAllActiveUserIds();
    }
