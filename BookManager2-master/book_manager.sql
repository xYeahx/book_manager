/*
 Navicat Premium Data Transfer

 Source Server         : xiaoye
 Source Server Type    : MySQL
 Source Server Version : 80403 (8.4.3)
 Source Host           : localhost:3306
 Source Schema         : book_manager

 Target Server Type    : MySQL
 Target Server Version : 80403 (8.4.3)
 File Encoding         : 65001

 Date: 21/05/2026 12:23:55
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for book_info
-- ----------------------------
DROP TABLE IF EXISTS `transaction`;

CREATE TABLE `transaction` (
  `transactionId` int NOT NULL AUTO_INCREMENT,
  `userId` int NOT NULL,
  `type` varchar(20) NOT NULL COMMENT '类型: recharge/deposit/refund/fine',
  `amount` decimal(10,2) NOT NULL COMMENT '金额（正=收入，负=支出）',
  `description` varchar(255) DEFAULT NULL,
  `createTime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`transactionId`),
  KEY `idx_userId` (`userId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 COMMENT='交易流水表';

DROP TABLE IF EXISTS `book_info`;
CREATE TABLE `book_info`  (
  `bookId` int NOT NULL AUTO_INCREMENT,
  `bookName` varchar(50) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL,
  `bookAuthor` varchar(50) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL,
  `bookPrice` decimal(10, 2) NOT NULL,
  `bookTypeId` int NOT NULL,
  `bookDesc` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT '书籍描述',
  `isBorrowed` tinyint NOT NULL COMMENT '1表示借出，0表示已还',
  `bookImg` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NULL DEFAULT NULL COMMENT '书籍图片',
  `isOffShelf` tinyint NOT NULL DEFAULT 0 COMMENT '0表示在架，1表示已下架',
  PRIMARY KEY (`bookId`) USING BTREE,
  INDEX `fk_book_info_book_type_1`(`bookTypeId` ASC) USING BTREE,
  CONSTRAINT `book_info_ibfk_1` FOREIGN KEY (`bookTypeId`) REFERENCES `book_type` (`bookTypeId`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 78 CHARACTER SET = utf8mb3 COLLATE = utf8mb3_general_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of book_info
-- ----------------------------
INSERT INTO `book_info` VALUES (1, '闪耀优俊少女', 'Cygames', 114.50, 1, '你说得对，但是闪耀优俊少女是由cygames自主研发的……', 0, 'http://localhost:8092/BookManager/pictures/1779171323318Suzuka.png', 0);
INSERT INTO `book_info` VALUES (2, '红楼梦', '曹雪芹', 36.00, 3, '《红楼梦》是一部百科全书式的长篇小说。以宝黛爱情悲剧为主线，以四大家族的荣辱兴衰为背景，描绘出18世纪中国封建社会的方方面面。', 0, '', 0);
INSERT INTO `book_info` VALUES (3, '你是一个一个一个', '野兽先辈', 114.50, 4, '1145141919810', 1, NULL, 0);
INSERT INTO `book_info` VALUES (4, '西游记', '吴承恩', 60.00, 3, '《西游记》主要描写的是孙悟空保唐僧西天取经，历经九九八十一难的故事。唐僧取经是历史上一件真实的事。大约距今一千三百多年前，即唐太宗贞观元年（627），年仅25岁的青年和尚玄奘离开京城长安，只身到天竺（印度）游学。他从长安出发后，途经中亚、阿富汗、巴基斯坦，历尽艰难险阻，最后到达了印度。他在那里学习了两年多，并在一次大型佛教经学辩论会任主讲，受到了赞誉。', 0, '', 0);
INSERT INTO `book_info` VALUES (72, '四只鸟之笔', '新蓝天', 38.00, 3, '四只鸟之笔，我是多大了', 1, '', 0);
INSERT INTO `book_info` VALUES (73, '凡人修仙传', '忘语', 35.00, 6, '凡人修仙传', 0, '', 0);
INSERT INTO `book_info` VALUES (74, '斗破苍穹', '天蚕土豆', 35.00, 6, '斗破苍穹，主角：萧炎', 1, '', 0);
INSERT INTO `book_info` VALUES (75, '吞噬星空', '我吃西红柿', 35.00, 6, '吞噬星空', 0, '', 0);
INSERT INTO `book_info` VALUES (76, '剑来', '烽火戏诸侯', 35.00, 6, '剑来', 0, '', 0);
INSERT INTO `book_info` VALUES (77, '哈利波特与魔法石', 'JK罗琳', 45.00, 7, '哈利波特系列第一部', 0, '', 0);

-- ----------------------------
-- Table structure for book_type
-- ----------------------------
DROP TABLE IF EXISTS `book_type`;
CREATE TABLE `book_type`  (
  `bookTypeId` int NOT NULL AUTO_INCREMENT,
  `bookTypeName` varchar(20) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL,
  `bookTypeDesc` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT '书籍类型描述',
  PRIMARY KEY (`bookTypeId`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 9 CHARACTER SET = utf8mb3 COLLATE = utf8mb3_general_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of book_type
-- ----------------------------
INSERT INTO `book_type` VALUES (1, '计算机科学', '计算机相关');
INSERT INTO `book_type` VALUES (2, '历史', '历史相关');
INSERT INTO `book_type` VALUES (3, '文学', '文学相关');
INSERT INTO `book_type` VALUES (4, '科幻', '科幻相关');
INSERT INTO `book_type` VALUES (6, '小说', '小说相关');
INSERT INTO `book_type` VALUES (7, '外语', '外语相关');

-- ----------------------------
-- Table structure for borrow
-- ----------------------------
DROP TABLE IF EXISTS `borrow`;
CREATE TABLE `borrow`  (
  `borrowId` int NOT NULL AUTO_INCREMENT,
  `userId` int NOT NULL,
  `bookId` int NOT NULL,
  `borrowTime` datetime NOT NULL,
  `returnTime` datetime NULL DEFAULT NULL,
  `renewCount` int NOT NULL DEFAULT 0 COMMENT '续借次数',
  `dueTime` datetime NULL DEFAULT NULL COMMENT '到期时间',
  PRIMARY KEY (`borrowId`) USING BTREE,
  INDEX `fk_borrow_user_1`(`userId` ASC) USING BTREE,
  INDEX `fk_borrow_book_info_1`(`bookId` ASC) USING BTREE,
  CONSTRAINT `borrow_ibfk_1` FOREIGN KEY (`bookId`) REFERENCES `book_info` (`bookId`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `borrow_ibfk_2` FOREIGN KEY (`userId`) REFERENCES `user` (`userId`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 64 CHARACTER SET = utf8mb3 COLLATE = utf8mb3_general_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of borrow
-- ----------------------------
INSERT INTO `borrow` VALUES (47, 21, 72, '2026-05-19 15:13:43', '2026-05-19 15:13:51', 0, '2026-06-02 15:13:43');
INSERT INTO `borrow` VALUES (48, 21, 73, '2026-05-21 10:25:04', '2026-05-21 10:25:30', 1, '2026-06-18 10:25:04');
INSERT INTO `borrow` VALUES (49, 21, 4, '2026-05-21 11:23:21', '2026-05-21 11:23:34', 1, '2026-06-18 11:23:21');
INSERT INTO `borrow` VALUES (50, 21, 72, '2026-05-21 11:24:28', '2026-05-21 11:24:48', 1, '2026-06-18 11:24:28');
INSERT INTO `borrow` VALUES (51, 21, 2, '2026-05-21 11:24:30', '2026-05-21 11:24:46', 0, NULL);
INSERT INTO `borrow` VALUES (52, 21, 75, '2026-05-21 11:24:33', '2026-05-21 11:24:49', 0, NULL);
INSERT INTO `borrow` VALUES (53, 21, 76, '2026-05-21 11:24:35', '2026-05-21 11:24:51', 0, NULL);
INSERT INTO `borrow` VALUES (54, 21, 1, '2026-05-21 11:26:46', '2026-05-21 11:27:12', 1, '2026-06-18 11:26:46');
INSERT INTO `borrow` VALUES (55, 21, 2, '2026-05-21 11:26:48', '2026-05-21 11:27:14', 0, NULL);
INSERT INTO `borrow` VALUES (56, 21, 3, '2026-05-21 11:26:49', '2026-05-21 11:27:21', 1, '2026-06-18 11:26:49');
INSERT INTO `borrow` VALUES (57, 21, 4, '2026-05-21 11:26:50', '2026-05-21 11:27:22', 0, NULL);
INSERT INTO `borrow` VALUES (58, 21, 1, '2026-05-21 11:38:20', '2026-05-21 12:00:21', 2, '2026-06-26 11:38:20');
INSERT INTO `borrow` VALUES (59, 21, 2, '2026-05-21 11:38:21', '2026-05-21 12:00:22', 0, NULL);
INSERT INTO `borrow` VALUES (60, 21, 3, '2026-05-21 11:38:22', '2026-05-21 12:00:23', 1, '2026-06-19 11:38:22');
INSERT INTO `borrow` VALUES (61, 21, 74, '2026-05-21 12:00:26', NULL, 0, NULL);
INSERT INTO `borrow` VALUES (62, 21, 72, '2026-05-21 12:00:29', NULL, 0, NULL);
INSERT INTO `borrow` VALUES (63, 21, 3, '2026-05-21 12:00:30', NULL, 1, '2026-06-19 12:00:30');

-- ----------------------------
-- Table structure for message
-- ----------------------------
DROP TABLE IF EXISTS `message`;
CREATE TABLE `message`  (
  `messageId` int NOT NULL AUTO_INCREMENT,
  `userId` int NOT NULL COMMENT '接收消息的用户ID',
  `title` varchar(100) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT '消息标题',
  `content` varchar(500) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT '消息内容',
  `isRead` tinyint NOT NULL DEFAULT 0 COMMENT '0未读，1已读',
  `createTime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '发送时间',
  PRIMARY KEY (`messageId`) USING BTREE,
  INDEX `idx_userId`(`userId` ASC) USING BTREE,
  INDEX `idx_createTime`(`createTime` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb3 COLLATE = utf8mb3_general_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of message
-- ----------------------------
INSERT INTO `message` VALUES (1, 21, '催还通知', '您好，您借阅的图书《红楼梦》已临近或超过归还期限，请尽快归还。如有疑问请联系管理员。', 0, '2026-05-21 11:43:10');

-- ----------------------------
-- Table structure for operation_log
-- ----------------------------
DROP TABLE IF EXISTS `operation_log`;
CREATE TABLE `operation_log`  (
  `logId` int NOT NULL AUTO_INCREMENT,
  `userId` int NOT NULL,
  `userName` varchar(20) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL,
  `operation` varchar(100) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL,
  `target` varchar(100) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NULL DEFAULT NULL,
  `createTime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`logId`) USING BTREE,
  INDEX `idx_userId`(`userId` ASC) USING BTREE,
  INDEX `idx_createTime`(`createTime` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 52 CHARACTER SET = utf8mb3 COLLATE = utf8mb3_general_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of operation_log
-- ----------------------------
INSERT INTO `operation_log` VALUES (1, 22, '周日', '上架图书', '四只鸟之笔', '2026-05-21 08:59:33');
INSERT INTO `operation_log` VALUES (2, 22, '周日', '下架图书', '凡人修仙传', '2026-05-21 09:00:04');
INSERT INTO `operation_log` VALUES (3, 22, '周日', '上架图书', '凡人修仙传', '2026-05-21 09:00:11');
INSERT INTO `operation_log` VALUES (4, 22, '周日', '下架图书', '红楼梦', '2026-05-21 09:13:23');
INSERT INTO `operation_log` VALUES (5, 22, '周日', '上架图书', '红楼梦', '2026-05-21 09:23:51');
INSERT INTO `operation_log` VALUES (6, 22, '周日', '新增图书', '斗破苍穹', '2026-05-21 09:24:14');
INSERT INTO `operation_log` VALUES (7, 22, '周日', '修改图书信息', '斗破苍穹', '2026-05-21 09:25:52');
INSERT INTO `operation_log` VALUES (8, 22, '周日', '修改图书信息', '四只鸟之笔', '2026-05-21 09:36:16');
INSERT INTO `operation_log` VALUES (9, 22, '周日', '删除图书类型', '美术', '2026-05-21 09:36:36');
INSERT INTO `operation_log` VALUES (10, 22, '周日', '新增图书', '吞噬星空', '2026-05-21 09:55:20');
INSERT INTO `operation_log` VALUES (11, 22, '周日', '新增图书', '剑来', '2026-05-21 09:55:35');
INSERT INTO `operation_log` VALUES (12, 21, '周一', '借出图书', '凡人修仙传', '2026-05-21 10:25:04');
INSERT INTO `operation_log` VALUES (13, 21, '周一', '续借图书', NULL, '2026-05-21 10:25:08');
INSERT INTO `operation_log` VALUES (14, 21, '周一', '归还图书', '凡人修仙传', '2026-05-21 10:25:30');
INSERT INTO `operation_log` VALUES (15, 22, '周日', '新增图书', '哈利波特与魔法石', '2026-05-21 10:27:17');
INSERT INTO `operation_log` VALUES (16, 21, '周一', '借出图书', '西游记', '2026-05-21 11:23:21');
INSERT INTO `operation_log` VALUES (17, 21, '周一', '续借图书', NULL, '2026-05-21 11:23:26');
INSERT INTO `operation_log` VALUES (18, 21, '周一', '归还图书', '西游记', '2026-05-21 11:23:34');
INSERT INTO `operation_log` VALUES (19, 21, '周一', '借出图书', '四只鸟之笔', '2026-05-21 11:24:28');
INSERT INTO `operation_log` VALUES (20, 21, '周一', '借出图书', '红楼梦', '2026-05-21 11:24:30');
INSERT INTO `operation_log` VALUES (21, 21, '周一', '借出图书', '吞噬星空', '2026-05-21 11:24:33');
INSERT INTO `operation_log` VALUES (22, 21, '周一', '借出图书', '剑来', '2026-05-21 11:24:35');
INSERT INTO `operation_log` VALUES (23, 21, '周一', '续借图书', NULL, '2026-05-21 11:24:42');
INSERT INTO `operation_log` VALUES (24, 21, '周一', '归还图书', '红楼梦', '2026-05-21 11:24:46');
INSERT INTO `operation_log` VALUES (25, 21, '周一', '归还图书', '四只鸟之笔', '2026-05-21 11:24:48');
INSERT INTO `operation_log` VALUES (26, 21, '周一', '归还图书', '吞噬星空', '2026-05-21 11:24:49');
INSERT INTO `operation_log` VALUES (27, 21, '周一', '归还图书', '剑来', '2026-05-21 11:24:51');
INSERT INTO `operation_log` VALUES (28, 21, '周一', '借出图书', '闪耀优俊少女', '2026-05-21 11:26:46');
INSERT INTO `operation_log` VALUES (29, 21, '周一', '借出图书', '红楼梦', '2026-05-21 11:26:48');
INSERT INTO `operation_log` VALUES (30, 21, '周一', '借出图书', '你是一个一个一个', '2026-05-21 11:26:49');
INSERT INTO `operation_log` VALUES (31, 21, '周一', '借出图书', '西游记', '2026-05-21 11:26:50');
INSERT INTO `operation_log` VALUES (32, 21, '周一', '续借图书', NULL, '2026-05-21 11:27:08');
INSERT INTO `operation_log` VALUES (33, 21, '周一', '归还图书', '闪耀优俊少女', '2026-05-21 11:27:12');
INSERT INTO `operation_log` VALUES (34, 21, '周一', '归还图书', '红楼梦', '2026-05-21 11:27:14');
INSERT INTO `operation_log` VALUES (35, 21, '周一', '续借图书', NULL, '2026-05-21 11:27:16');
INSERT INTO `operation_log` VALUES (36, 21, '周一', '归还图书', '你是一个一个一个', '2026-05-21 11:27:21');
INSERT INTO `operation_log` VALUES (37, 21, '周一', '归还图书', '西游记', '2026-05-21 11:27:22');
INSERT INTO `operation_log` VALUES (38, 21, '周一', '借出图书', '闪耀优俊少女', '2026-05-21 11:38:20');
INSERT INTO `operation_log` VALUES (39, 21, '周一', '借出图书', '红楼梦', '2026-05-21 11:38:21');
INSERT INTO `operation_log` VALUES (40, 21, '周一', '借出图书', '你是一个一个一个', '2026-05-21 11:38:22');
INSERT INTO `operation_log` VALUES (41, 21, '周一', '续借图书', NULL, '2026-05-21 11:41:09');
INSERT INTO `operation_log` VALUES (42, 21, '周一', '续借图书', NULL, '2026-05-21 11:41:11');
INSERT INTO `operation_log` VALUES (43, 21, '周一', '续借图书', NULL, '2026-05-21 11:41:19');
INSERT INTO `operation_log` VALUES (44, 26, '周继业', '发送催还通知', '红楼梦', '2026-05-21 11:43:10');
INSERT INTO `operation_log` VALUES (45, 21, '周一', '归还图书', '闪耀优俊少女', '2026-05-21 12:00:21');
INSERT INTO `operation_log` VALUES (46, 21, '周一', '归还图书', '红楼梦', '2026-05-21 12:00:22');
INSERT INTO `operation_log` VALUES (47, 21, '周一', '归还图书', '你是一个一个一个', '2026-05-21 12:00:23');
INSERT INTO `operation_log` VALUES (48, 21, '周一', '借出图书', '斗破苍穹', '2026-05-21 12:00:26');
INSERT INTO `operation_log` VALUES (49, 21, '周一', '借出图书', '四只鸟之笔', '2026-05-21 12:00:29');
INSERT INTO `operation_log` VALUES (50, 21, '周一', '借出图书', '你是一个一个一个', '2026-05-21 12:00:30');
INSERT INTO `operation_log` VALUES (51, 21, '周一', '续借图书', NULL, '2026-05-21 12:00:36');

-- ----------------------------
-- Table structure for system_config
-- ----------------------------
DROP TABLE IF EXISTS `system_config`;
CREATE TABLE `system_config`  (
  `configId` int NOT NULL AUTO_INCREMENT,
  `configKey` varchar(50) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL,
  `configValue` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL,
  `configDesc` varchar(100) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NULL DEFAULT NULL,
  PRIMARY KEY (`configId`) USING BTREE,
  UNIQUE INDEX `uk_configKey`(`configKey` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 19 CHARACTER SET = utf8mb3 COLLATE = utf8mb3_general_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of system_config
-- ----------------------------
INSERT INTO `system_config` VALUES (1, 'borrow_days', '22', '借阅期限（天）');
INSERT INTO `system_config` VALUES (2, 'max_renew_count', '2', '最大续借次数');
INSERT INTO `system_config` VALUES (3, 'invite_code', 'xiaoye123', '管理员注册邀请码');
INSERT INTO `system_config` VALUES (4, 'max_borrow_count', '3', '每人最大借阅数量');
INSERT INTO `system_config` VALUES (5, 'renew_days', '7', '续借延长天数');
INSERT INTO `system_config` VALUES (6, 'allow_register', 'false', '允许开放注册');
INSERT INTO `system_config` VALUES (7, 'default_role', '0', '新用户默认角色');
INSERT INTO `system_config` VALUES (8, 'remind_days', '3', '到期前提醒天数');
INSERT INTO `system_config` VALUES (9, 'fine_amount', '0.5', '逾期罚款金额');
INSERT INTO `system_config` VALUES (10, 'enable_notification', 'true', '启用消息推送');
INSERT INTO `system_config` VALUES (11, 'library_name', '', '图书馆名称');
INSERT INTO `system_config` VALUES (12, 'contact_phone', '', '联系电话');
INSERT INTO `system_config` VALUES (13, 'opening_hours', '', '开放时间说明');
INSERT INTO `system_config` VALUES (14, 'system_notice', '', '系统公告');
INSERT INTO `system_config` VALUES (15, 'min_password_length', '8', '密码最小长度');
INSERT INTO `system_config` VALUES (16, 'max_login_attempts', '5', '登录失败锁定次数');
INSERT INTO `system_config` VALUES (17, 'force_change_password', 'true', '强制修改初始密码');
INSERT INTO `system_config` VALUES (18, 'log_retention_days', '90', '日志保留天数');

-- ----------------------------
-- Table structure for user
-- ----------------------------
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user`  (
  `userId` int NOT NULL AUTO_INCREMENT,
  `userName` varchar(20) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL,
  `userPassword` varchar(20) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL,
  `isAdmin` tinyint NOT NULL COMMENT '0是读者，1是管理员，2是超级管理员',
  `balance` decimal(10,2) NOT NULL DEFAULT 0.00 COMMENT '账户余额（押金）',
  PRIMARY KEY (`userId`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 30 CHARACTER SET = utf8mb3 COLLATE = utf8mb3_general_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of user
-- ----------------------------
INSERT INTO `user` VALUES (21, '周一', '123456', 0, 0.00);
INSERT INTO `user` VALUES (22, '周日', '123456', 1, 0.00);
INSERT INTO `user` VALUES (23, '周六', '123456', 1, 0.00);
INSERT INTO `user` VALUES (24, '周二', '123456', 0, 0.00);
INSERT INTO `user` VALUES (25, '周八', '123456', 1, 0.00);
INSERT INTO `user` VALUES (26, '周继业', '123456', 2, 0.00);
INSERT INTO `user` VALUES (27, '周三', '123456', 0, 0.00);
INSERT INTO `user` VALUES (28, '周四', '123456', 0, 0.00);
INSERT INTO `user` VALUES (29, '周五', '123456', 0, 0.00);

SET FOREIGN_KEY_CHECKS = 1;
