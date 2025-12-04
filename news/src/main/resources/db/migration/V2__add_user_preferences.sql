-- 用户偏好表
-- Telegram用户个性化订阅和推送偏好管理

CREATE TABLE IF NOT EXISTS user_preferences (
    user_id BIGINT PRIMARY KEY COMMENT 'Telegram用户ID',
    keywords TEXT COMMENT '订阅关键词，逗号分隔',
    push_frequency INT NOT NULL DEFAULT 30 COMMENT '推送频率（分钟）',
    push_start_time TIME NOT NULL DEFAULT '09:00:00' COMMENT '推送开始时间',
    push_end_time TIME NOT NULL DEFAULT '22:00:00' COMMENT '推送结束时间',
    is_enabled BOOLEAN NOT NULL DEFAULT true COMMENT '是否启用推送',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    last_push_at TIMESTAMP NULL COMMENT '上次推送时间',
    push_count INT NOT NULL DEFAULT 0 COMMENT '推送次数统计'
) COMMENT='用户偏好设置表';

-- 创建索引
CREATE INDEX idx_user_preferences_enabled ON user_preferences(is_enabled);
CREATE INDEX idx_user_preferences_last_push ON user_preferences(last_push_at);
CREATE INDEX idx_user_preferences_updated ON user_preferences(updated_at);