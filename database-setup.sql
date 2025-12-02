-- Daily News 项目数据库初始化脚本
-- 适用于 Replit MySQL 环境

-- 创建数据库
CREATE DATABASE IF NOT EXISTS `daily-news` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS `financial_analytics` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 使用 daily-news 数据库
USE `daily-news`;

-- 创建新闻表
CREATE TABLE IF NOT EXISTS `news` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `site_source` varchar(255) DEFAULT NULL COMMENT '来源站点',
  `publish_time` bigint DEFAULT NULL COMMENT '发布时间戳',
  `status` int DEFAULT '0' COMMENT '状态：0-新入库，1-已发布',
  `title` varchar(500) DEFAULT NULL COMMENT '标题',
  `link` varchar(500) DEFAULT NULL COMMENT '链接',
  `tags` text COMMENT '标签JSON',
  `content` longtext COMMENT '内容',
  PRIMARY KEY (`id`),
  KEY `idx_publish_time` (`publish_time`),
  KEY `idx_status` (`status`),
  KEY `idx_site_source` (`site_source`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='新闻表';

-- 使用 financial_analytics 数据库
USE `financial_analytics`;

-- 创建比特币实体汇总表
CREATE TABLE IF NOT EXISTS `bitcoin_entities_summary` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `date` date DEFAULT NULL COMMENT '日期',
  `total_entities` int DEFAULT NULL COMMENT '总实体数',
  `total_balance` decimal(20,8) DEFAULT NULL COMMENT '总余额',
  `created_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_date` (`date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='比特币实体汇总';

-- 创建比特币持有量统计表
CREATE TABLE IF NOT EXISTS `bitcoin_holdings` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `date` date DEFAULT NULL COMMENT '日期',
  `category` varchar(100) DEFAULT NULL COMMENT '分类',
  `entities_count` int DEFAULT NULL COMMENT '实体数量',
  `balance` decimal(20,8) DEFAULT NULL COMMENT '余额',
  `percentage` decimal(5,2) DEFAULT NULL COMMENT '百分比',
  `created_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_date` (`date`),
  KEY `idx_category` (`category`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='比特币持有量统计';

-- 创建比特币实体明细表
CREATE TABLE IF NOT EXISTS `bitcoin_entities_detail` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `date` date DEFAULT NULL COMMENT '日期',
  `entity_name` varchar(255) DEFAULT NULL COMMENT '实体名称',
  `category` varchar(100) DEFAULT NULL COMMENT '分类',
  `balance` decimal(20,8) DEFAULT NULL COMMENT '余额',
  `percentage` decimal(5,2) DEFAULT NULL COMMENT '百分比',
  `created_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_date` (`date`),
  KEY `idx_entity_name` (`entity_name`),
  KEY `idx_category` (`category`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='比特币实体明细';

-- 插入测试数据
USE `daily-news`;
INSERT INTO `news` (`site_source`, `publish_time`, `status`, `title`, `link`, `tags`, `content`) VALUES
('binance', UNIX_TIMESTAMP(NOW()), 0, '比特币价格突破新高', 'https://binance.com/news/bitcoin-new-high', '["bitcoin", "price"]', '比特币价格今日突破历史新高，市场情绪积极'),
('coinbase', UNIX_TIMESTAMP(NOW()), 0, '以太坊2.0升级完成', 'https://coinbase.com/news/eth2-upgrade', '["ethereum", "upgrade"]', '以太坊2.0升级顺利完成，网络性能大幅提升');

-- 授权root用户远程访问（Replit环境需要）
GRANT ALL PRIVILEGES ON *.* TO 'root'@'%' IDENTIFIED BY 'root123' WITH GRANT OPTION;
FLUSH PRIVILEGES;