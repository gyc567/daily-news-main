package com.ll.news.repository;

import com.ll.news.entity.UserPreference;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 用户偏好数据访问层
 * 使用Spring Data JPA，遵循Linus的简洁原则
 */
@Repository
public interface UserPreferenceRepository extends JpaRepository<UserPreference, Long> {

    /**
     * 查询启用的用户偏好
     */
    @Query("SELECT u FROM UserPreference u WHERE u.enabled = true")
    List<UserPreference> findAllByEnabledTrue();

    /**
     * 按用户ID查询
     */
    List<UserPreference> findByUserId(Long userId);

    /**
     * 查询启用的用户
     */
    List<UserPreference> findByEnabledTrue();

    /**
     * 检查用户是否存在
     */
    boolean existsByUserId(Long userId);
}