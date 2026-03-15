package com.chaing.core.repository;

import com.chaing.core.dto.TargetType;
import com.chaing.core.entity.Image;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ImageRepository extends JpaRepository<Image, Long> {

    List<Image> findAllByTargetTypeAndTargetId(TargetType targetType, Long targetId);
    void deleteAllByTargetTypeAndTargetId(TargetType targetType, Long targetId);
    void deleteByStoredName(String storedName);
    boolean existsByStoredNameAndTargetTypeAndTargetId(String storedName, TargetType targetType, Long targetId);
}
