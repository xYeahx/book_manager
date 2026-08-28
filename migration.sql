-- ============================================================
-- 迁移脚本：给已有数据库添加余额/押金系统
-- 使用方法：直接全部执行即可，个别报错可忽略
-- ============================================================

-- 第1步：添加 balance 列（若已存在会报 Duplicate column，忽略即可）
ALTER TABLE user ADD COLUMN balance decimal(10,2) NOT NULL DEFAULT 0.00;

-- 第2步：创建 transaction 流水表
CREATE TABLE IF NOT EXISTS `transaction` (
  `transactionId` int NOT NULL AUTO_INCREMENT,
  `userId` int NOT NULL,
  `type` varchar(20) NOT NULL COMMENT 'type',
  `amount` decimal(10,2) NOT NULL COMMENT 'amount',
  `description` varchar(255) DEFAULT NULL,
  `createTime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`transactionId`),
  KEY `idx_userId` (`userId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3;

-- 第3步：插入罚款配置
INSERT IGNORE INTO system_config (config_key, config_value, description)
VALUES ('fine_amount', '0.50', 'overdue fine per day');
