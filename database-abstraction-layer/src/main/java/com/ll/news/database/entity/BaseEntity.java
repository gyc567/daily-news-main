/**
 * 基础实体类
 * 提供所有实体共有的字段和行为
 * 遵循KISS原则：只包含最必要的通用字段
 * 高内聚：所有实体通用逻辑集中定义
 */
package com.ll.news.database.entity;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 基础实体类
 * 所有实体类都应该继承此类
 */
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
public abstract class BaseEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * ID主键 - 使用数据库自增策略
     * 低耦合：不依赖具体数据库的自增实现
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 创建时间
     * 自动填充，无需手动设置
     */
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * 最后修改时间
     * 自动更新
     */
    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * 创建者
     * 可以通过 AuditorAware 配置自动填充
     */
    @CreatedBy
    @Column(name = "created_by", length = 100)
    private String createdBy;

    /**
     * 最后修改者
     */
    @LastModifiedBy
    @Column(name = "updated_by", length = 100)
    private String updatedBy;

    /**
     * 版本号 - 用于乐观锁
     * 支持并发更新
     */
    @Version
    @Column(name = "version")
    private Long version = 0L;

    /**
     * 是否删除标记
     * 实现软删除功能
     */
    @Column(name = "deleted", nullable = false)
    private Boolean deleted = false;

    /**
     * 删除时间
     * 软删除时自动设置
     */
    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    /**
     * 删除者
     * 记录谁删除了这条数据
     */
    @Column(name = "deleted_by", length = 100)
    private String deletedBy;

    /**
     * 重写equals方法 - 基于业务键而非ID
     * 保证在ID为null时的正确性
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        BaseEntity that = (BaseEntity) obj;

        // 如果ID都不为null，比较ID
        if (id != null && that.id != null) {
            return id.equals(that.id);
        }

        // 如果ID为null，比较业务键（子类实现）
        return equalsByBusinessKey(that);
    }

    /**
     * 重写hashCode方法
     */
    @Override
    public int hashCode() {
        if (id != null) {
            return id.hashCode();
        }
        // ID为null时，基于业务键计算hashCode
        return hashCodeByBusinessKey();
    }

    /**
     * 基于业务键判断是否相等
     * 子类可以重写此方法以实现业务键比较
     */
    protected boolean equalsByBusinessKey(BaseEntity other) {
        // 默认实现：直接比较对象引用
        return this == other;
    }

    /**
     * 基于业务键计算hashCode
     * 子类可以重写此方法
     */
    protected int hashCodeByBusinessKey() {
        // 默认实现：基于类名计算
        return getClass().hashCode();
    }

    /**
     * 标记为删除
     * 实现软删除功能
     */
    public void markAsDeleted(String deletedBy) {
        this.deleted = true;
        this.deletedAt = LocalDateTime.now();
        this.deletedBy = deletedBy;
    }

    /**
     * 恢复删除的数据
     */
    public void markAsRestored() {
        this.deleted = false;
        this.deletedAt = null;
        this.deletedBy = null;
    }

    /**
     * 检查是否是新实体（ID为null）
     */
    public boolean isNew() {
        return id == null;
    }

    /**
     * 检查是否已被删除
     */
    public boolean isDeleted() {
        return Boolean.TRUE.equals(deleted);
    }

    /**
     * 获取实体名称 - 用于日志和异常信息
     */
    public String getEntityName() {
        return getClass().getSimpleName();
    }
}

/**
 * 可审计实体接口
 * 用于标识支持审计功能的实体
 */
interface Auditable {
    LocalDateTime getCreatedAt();
    void setCreatedAt(LocalDateTime createdAt);

    LocalDateTime getUpdatedAt();
    void setUpdatedAt(LocalDateTime updatedAt);

    String getCreatedBy();
    void setCreatedBy(String createdBy);

    String getUpdatedBy();
    void setUpdatedBy(String updatedBy);
}

/**
 * 软删除接口
 * 用于标识支持软删除功能的实体
 */
interface SoftDeletable {
    boolean isDeleted();
    void setDeleted(boolean deleted);

    LocalDateTime getDeletedAt();
    void setDeletedAt(LocalDateTime deletedAt);

    String getDeletedBy();
    void setDeletedBy(String deletedBy);
}

/**
 * 使用示例：
 *
 * ```java
 * @Entity
 * @Table(name = "news", schema = "news")
 * public class News extends BaseEntity {
 *
 *     @Column(name = "title")
 *     private String title;
 *
 *     @Override
 *     protected boolean equalsByBusinessKey(BaseEntity other) {
 *         if (other instanceof News) {
 *             News that = (News) other;
 *             // 基于标题和发布时间比较
 *             return Objects.equals(this.title, that.title) &&
 *                    Objects.equals(this.publishTime, that.publishTime);
 *         }
 *         return false;
 *     }
 *
 *     @Override
 *     protected int hashCodeByBusinessKey() {
 *         return Objects.hash(title, publishTime);
 *     }
 * }
 * ```
 */