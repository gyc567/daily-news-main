package com.ll.news.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * news
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@TableName(value = "news")
public class News {
    /**
     * 唯一id
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 来源
     */
    @TableField(value = "site_source")
    private String siteSource;

    /**
     * 发布时间
     */
    @TableField(value = "publish_time")
    private Long publishTime;

    /**
     * 状态， 0 新入库，1 已发布
     */
    @TableField(value = "`status`")
    private Integer status;

    /**
     * 标题
     */
    @TableField(value = "title")
    private String title;

    /**
     * 连接
     */
    @TableField(value = "link")
    private String link;

    /**
     * 标签
     */
    @TableField(value = "tags")
    private String tags;

    /**
     * 内容
     */
    @TableField(value = "content")
    private String content;
}
