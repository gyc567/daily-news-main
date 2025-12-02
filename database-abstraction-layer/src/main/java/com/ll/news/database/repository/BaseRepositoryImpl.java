/**
 * 基础Repository实现
 * 高内聚：通用的数据访问逻辑集中实现
 * 低耦合：业务Repository只依赖此抽象，不依赖具体数据库
 *
 * 遵循KISS原则：只提供最常用、最必要的通用方法
 */
package com.ll.news.database.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.support.JpaEntityInformation;
import org.springframework.data.jpa.repository.support.SimpleJpaRepository;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 基础Repository实现类
 * 提供通用的数据访问功能，支持多种数据库
 */
@NoRepositoryBean
public class BaseRepositoryImpl<T, ID extends Serializable> extends SimpleJpaRepository<T, ID>
        implements BaseRepository<T, ID> {

    private final EntityManager entityManager;
    private final Class<T> domainClass;

    public BaseRepositoryImpl(JpaEntityInformation<T, ?> entityInformation, EntityManager entityManager) {
        super(entityInformation, entityManager);
        this.entityManager = entityManager;
        this.domainClass = entityInformation.getJavaType();
    }

    /**
     * 软删除 - 遵循KISS原则，最常用的功能
     */
    @Override
    @Transactional
    public void softDelete(T entity) {
        if (entity instanceof SoftDeletable) {
            SoftDeletable deletable = (SoftDeletable) entity;
            deletable.setDeleted(true);
            deletable.setDeletedAt(LocalDateTime.now());
            entityManager.merge(entity);
        } else {
            throw new UnsupportedOperationException("Entity does not support soft delete: " + domainClass.getName());
        }
    }

    /**
     * 批量软删除
     */
    @Override
    @Transactional
    public void softDeleteAll(Iterable<? extends T> entities) {
        entities.forEach(this::softDelete);
    }

    /**
     * 查找未删除的实体 - 自动过滤已删除数据
     */
    @Override
    @Transactional(readOnly = true)
    public List<T> findAllActive() {
        if (SoftDeletable.class.isAssignableFrom(domainClass)) {
            return entityManager.createQuery(
                    "SELECT e FROM " + domainClass.getSimpleName() + " e WHERE e.deleted = false", domainClass)
                    .getResultList();
        }
        return findAll();
    }

    /**
     * 分页查找未删除的实体
     */
    @Override
    @Transactional(readOnly = true)
    public Page<T> findAllActive(Pageable pageable) {
        if (SoftDeletable.class.isAssignableFrom(domainClass)) {
            return findAllActive(pageable);
        }
        return findAll(pageable);
    }

    /**
     * 根据ID查找未删除的实体
     */
    @Override
    @Transactional(readOnly = true)
    public Optional<T> findByIdActive(ID id) {
        Optional<T> entity = findById(id);
        if (entity.isPresent() && entity.get() instanceof SoftDeletable) {
            SoftDeletable deletable = (SoftDeletable) entity.get();
            return deletable.isDeleted() ? Optional.empty() : entity;
        }
        return entity;
    }

    /**
     * 检查实体是否存在且未删除
     */
    @Override
    @Transactional(readOnly = true)
    public boolean existsByIdActive(ID id) {
        Optional<T> entity = findById(id);
        if (entity.isPresent() && entity.get() instanceof SoftDeletable) {
            SoftDeletable deletable = (SoftDeletable) entity.get();
            return !deletable.isDeleted();
        }
        return entity.isPresent();
    }

    /**
     * 刷新实体状态 - 支持多种数据库的通用刷新
     */
    @Override
    @Transactional
    public void refresh(T entity) {
        entityManager.refresh(entity);
    }

    /**
     * 批量刷新实体状态
     */
    @Override
    @Transactional
    public void refreshAll(Iterable<T> entities) {
        entities.forEach(this::refresh);
    }

    /**
     * 获取实体管理器 - 用于复杂查询
     */
    @Override
    public EntityManager getEntityManager() {
        return entityManager;
    }

    /**
     * 执行原生SQL查询 - 数据库抽象的最后手段
     * 警告：使用原生SQL会降低数据库可移植性
     */
    @Override
    @Transactional
    public int executeNativeQuery(String sql, Object... params) {
        var query = entityManager.createNativeQuery(sql);
        for (int i = 0; i < params.length; i++) {
            query.setParameter(i + 1, params[i]);
        }
        return query.executeUpdate();
    }

    /**
     * 执行原生SQL查询并返回结果
     */
    @Override
    @Transactional(readOnly = true)
    @SuppressWarnings("unchecked")
    public <R> List<R> executeNativeQuery(String sql, Class<R> resultClass, Object... params) {
        var query = entityManager.createNativeQuery(sql, resultClass);
        for (int i = 0; i < params.length; i++) {
            query.setParameter(i + 1, params[i]);
        }
        return query.getResultList();
    }
}

/**
 * 软删除接口
 * 实现此接口的实体支持软删除功能
 */
interface SoftDeletable {
    boolean isDeleted();
    void setDeleted(boolean deleted);
    LocalDateTime getDeletedAt();
    void setDeletedAt(LocalDateTime deletedAt);
}