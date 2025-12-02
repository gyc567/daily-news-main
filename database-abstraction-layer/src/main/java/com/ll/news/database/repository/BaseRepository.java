/**
 * 基础Repository接口
 * 定义通用的数据访问操作，支持多种数据库
 * 遵循KISS原则：只定义最常用、最必要的操作
 */
package com.ll.news.database.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import javax.persistence.EntityManager;
import java.io.Serializable;
import java.util.List;
import java.util.Optional;

/**
 * 基础Repository接口
 * 扩展Spring Data JPA的JpaRepository，添加通用功能
 */
@NoRepositoryBean
public interface BaseRepository<T, ID extends Serializable> extends JpaRepository<T, ID> {

    /**
     * 软删除 - 标记为已删除但不从数据库中移除
     * KISS：最常用的删除方式，支持数据恢复
     */
    void softDelete(T entity);

    /**
     * 批量软删除
     */
    void softDeleteAll(Iterable<? extends T> entities);

    /**
     * 查找所有未删除的实体
     * 自动过滤已软删除的数据
     */
    List<T> findAllActive();

    /**
     * 分页查找所有未删除的实体
     */
    Page<T> findAllActive(Pageable pageable);

    /**
     * 根据ID查找未删除的实体
     */
    Optional<T> findByIdActive(ID id);

    /**
     * 检查实体是否存在且未删除
     */
    boolean existsByIdActive(ID id);

    /**
     * 刷新实体状态 - 从数据库重新加载
     * 保证数据一致性
     */
    void refresh(T entity);

    /**
     * 批量刷新实体状态
     */
    void refreshAll(Iterable<T> entities);

    /**
     * 获取实体管理器
     * 用于执行复杂的JPA查询
     * 使用场景：当Spring Data方法无法满足需求时
     */
    EntityManager getEntityManager();

    /**
     * 执行原生SQL更新/删除操作
     * 警告：会降低数据库可移植性，谨慎使用
     * @param sql 原生SQL语句
     * @param params 参数列表
     * @return 影响的行数
     */
    int executeNativeQuery(String sql, Object... params);

    /**
     * 执行原生SQL查询操作
     * 警告：会降低数据库可移植性，谨慎使用
     * @param sql 原生SQL语句
     * @param resultClass 结果类型
     * @param params 参数列表
     * @param <R> 返回结果类型
     * @return 查询结果列表
     */
    <R> List<R> executeNativeQuery(String sql, Class<R> resultClass, Object... params);
}

/**
 * 数据库类型枚举
 * 支持项目可能使用的各种数据库
 */
enum DatabaseType {
    MYSQL,
    POSTGRESQL,
    NEON,      // Neon Serverless PostgreSQL
    H2          // 主要用于测试
}

/**
 * 使用示例：
 *
 * ```java
 * @Repository
 * public interface NewsRepository extends BaseRepository<News, Long> {
 *     // 继承所有基础方法
 *
 *     // 添加业务特定方法
 *     List<News> findByStatusOrderByPublishTimeDesc(NewsStatus status);
 * }
 *
 * @Service
 * public class NewsService {
 *     private final NewsRepository newsRepository;
 *
 *     public void deleteNews(Long id) {
 *         News news = newsRepository.findById(id)
 *             .orElseThrow(() -> new NewsNotFoundException(id));
 *
 *         // 使用软删除而非硬删除
 *         newsRepository.softDelete(news);
 *     }
 * }
 * ```
 */