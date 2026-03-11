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

        @Query(value = """
            SELECT employee_number
            FROM user
            WHERE role = :#{#role.name()}
            ORDER BY employee_number DESC
            LIMIT 1
            FOR UPDATE
            """, nativeQuery = true)
        Optional<String> findMaxEmployeeNumberByRole(@Param("role") UserRole role);

        @Query(value = """
            SELECT login_id
            FROM user
            WHERE login_id LIKE CONCAT(:pattern, '%')
            ORDER BY login_id DESC
            LIMIT 1
            FOR UPDATE
            """, nativeQuery = true)
        Optional<String> findMaxLoginIdByPattern(@Param("pattern") String pattern);

        boolean existsByEmail(String email);

        @Query("SELECT u.userId FROM User u WHERE u.status = com.chaing.domain.users.enums.UserStatus.ACTIVE")
        List<Long> getAllActiveUserIds();

        @Query("SELECT DISTINCT u.businessUnitId FROM User u WHERE u.role = :role AND u.businessUnitId IS NOT NULL")
        List<Long> findDistinctBusinessUnitIdsByRole(@Param("role") UserRole role);
}
