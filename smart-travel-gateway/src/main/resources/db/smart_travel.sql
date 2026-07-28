CREATE DATABASE IF NOT EXISTS smart_travel DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE smart_travel;

-- ============================================
-- 用户模块
-- ============================================

-- 旅友表
DROP TABLE IF EXISTS `tb_traveler`;
CREATE TABLE `tb_traveler` (
  `id` bigint(20) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  `phone` varchar(11) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '手机号码',
  `password` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '密码，加密存储',
  `nick_name` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '昵称',
  `token_version` int(11) NOT NULL DEFAULT 0 COMMENT 'Token版本号，改密码时自增，用于旧Token失效',
  `icon` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '头像',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_phone`(`phone`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1001 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '旅友表' ROW_FORMAT = Compact;

-- 旅友详情表
DROP TABLE IF EXISTS `tb_traveler_info`;
CREATE TABLE `tb_traveler_info` (
  `user_id` bigint(20) UNSIGNED NOT NULL COMMENT '主键，用户id',
  `city` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '所在城市',
  `introduce` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '个人介绍',
  `fans` int(8) UNSIGNED NULL DEFAULT 0 COMMENT '粉丝数量',
  `followee` int(8) UNSIGNED NULL DEFAULT 0 COMMENT '关注数量',
  `gender` tinyint(1) UNSIGNED NULL DEFAULT 0 COMMENT '性别，0：男，1：女',
  `birthday` date NULL DEFAULT NULL COMMENT '生日',
  `credits` int(10) UNSIGNED NULL DEFAULT 0 COMMENT '积分',
  `level` tinyint(2) UNSIGNED NULL DEFAULT 0 COMMENT '会员级别，0~9级',
  `balance` bigint(10) UNSIGNED NULL DEFAULT 0 COMMENT '余额（分）',
  `create_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`user_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '旅友详情表' ROW_FORMAT = Compact;

-- ============================================
-- 景点模块
-- ============================================

-- 景点分类表
DROP TABLE IF EXISTS `tb_scenic_type`;
CREATE TABLE `tb_scenic_type` (
  `id` bigint(20) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  `name` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '分类名称',
  `icon` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '图标',
  `sort` int(3) UNSIGNED NULL DEFAULT 0 COMMENT '排序',
  `create_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_sort`(`sort`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '景点分类表' ROW_FORMAT = Compact;

-- 景点表
DROP TABLE IF EXISTS `tb_scenic`;
CREATE TABLE `tb_scenic` (
  `id` bigint(20) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  `name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '景点名称',
  `type_id` bigint(20) UNSIGNED NOT NULL COMMENT '分类id',
  `images` varchar(2048) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '图片列表，逗号分隔',
  `area` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '所属区域',
  `address` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '详细地址',
  `x` double NOT NULL COMMENT '经度',
  `y` double NOT NULL COMMENT '纬度',
  `avg_price` bigint(10) UNSIGNED NULL DEFAULT 0 COMMENT '均价（分）',
  `sold` int(10) UNSIGNED NULL DEFAULT 0 COMMENT '销量',
  `comments` int(10) UNSIGNED NULL DEFAULT 0 COMMENT '评论数量',
  `score` int(2) UNSIGNED NULL DEFAULT 0 COMMENT '评分，1~50',
  `open_hours` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '营业时间',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '景点描述',
  `tags` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '标签，逗号分隔',
  `status` tinyint(1) UNSIGNED NULL DEFAULT 1 COMMENT '状态，0：关闭，1：开放',
  `create_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_type_id`(`type_id`) USING BTREE,
  INDEX `idx_score`(`score`) USING BTREE,
  INDEX `idx_area`(`area`) USING BTREE,
  INDEX `idx_status`(`status`) USING BTREE,
  -- 空间索引仅绑定POINT单字段
  SPATIAL INDEX `idx_location`(`location`)
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '景点表' ROW_FORMAT = Compact;

-- 景点图片表
DROP TABLE IF EXISTS `tb_scenic_image`;
CREATE TABLE `tb_scenic_image` (
  `id` bigint(20) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  `scenic_id` bigint(20) UNSIGNED NOT NULL COMMENT '景点id',
  `image_url` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '图片URL',
  `sort` int(4) UNSIGNED NULL DEFAULT 0 COMMENT '排序',
  `is_cover` tinyint(1) UNSIGNED NULL DEFAULT 0 COMMENT '是否封面',
  `create_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_scenic_id`(`scenic_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '景点图片表' ROW_FORMAT = Compact;

-- ============================================
-- 门票模块
-- ============================================

-- 门票表
DROP TABLE IF EXISTS `tb_ticket`;
CREATE TABLE `tb_ticket` (
  `id` bigint(20) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  `scenic_id` bigint(20) UNSIGNED NOT NULL COMMENT '景点id',
  `title` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '门票标题',
  `sub_title` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '副标题',
  `rules` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '使用规则',
  `pay_value` bigint(10) UNSIGNED NOT NULL COMMENT '支付金额（分）',
  `actual_value` bigint(10) UNSIGNED NOT NULL COMMENT '实际价值（分）',
  `type` tinyint(1) UNSIGNED NULL DEFAULT 1 COMMENT '类型，1：普通门票，2：秒杀门票',
  `status` tinyint(1) UNSIGNED NULL DEFAULT 1 COMMENT '状态，0：下架，1：上架',
  `stock` int(8) UNSIGNED NULL DEFAULT 0 COMMENT '库存',
  `valid_start` timestamp NULL DEFAULT NULL COMMENT '有效期开始',
  `valid_end` timestamp NULL DEFAULT NULL COMMENT '有效期结束',
  `create_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_scenic_id`(`scenic_id`) USING BTREE,
  INDEX `idx_type`(`type`) USING BTREE,
  INDEX `idx_status`(`status`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '门票表' ROW_FORMAT = Compact;

-- 秒杀门票表
DROP TABLE IF EXISTS `tb_seckill_ticket`;
CREATE TABLE `tb_seckill_ticket` (
  `ticket_id` bigint(20) UNSIGNED NOT NULL COMMENT '门票id',
  `stock` int(8) NOT NULL COMMENT '秒杀库存',
  `begin_time` timestamp NOT NULL COMMENT '秒杀开始时间',
  `end_time` timestamp NOT NULL COMMENT '秒杀结束时间',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`ticket_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '秒杀门票表' ROW_FORMAT = Compact;

-- 门票订单表
DROP TABLE IF EXISTS `tb_ticket_order`;
CREATE TABLE `tb_ticket_order` (
  `id` bigint(20) UNSIGNED NOT NULL COMMENT '主键（雪花ID）',
  `user_id` bigint(20) UNSIGNED NOT NULL COMMENT '用户id',
  `ticket_id` bigint(20) UNSIGNED NOT NULL COMMENT '门票id',
  `scenic_id` bigint(20) UNSIGNED NOT NULL COMMENT '景点id',
  `count` int(4) UNSIGNED NULL DEFAULT 1 COMMENT '购买数量',
  `pay_amount` bigint(10) UNSIGNED NOT NULL COMMENT '支付金额（分）',
  `pay_type` tinyint(1) UNSIGNED NULL DEFAULT 1 COMMENT '支付方式，1：余额，2：支付宝，3：微信',
  `status` tinyint(1) UNSIGNED NULL DEFAULT 1 COMMENT '状态，1：未支付，2：已支付，3：已核销，4：已取消，5：退款中，6：已退款',
  `order_no` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '订单号',
  `create_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `pay_time` timestamp NULL DEFAULT NULL COMMENT '支付时间',
  `use_time` timestamp NULL DEFAULT NULL COMMENT '核销时间',
  `refund_time` timestamp NULL DEFAULT NULL COMMENT '退款时间',
  `update_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_user_id`(`user_id`) USING BTREE,
  INDEX `idx_ticket_id`(`ticket_id`) USING BTREE,
  INDEX `idx_status`(`status`) USING BTREE,
  INDEX `idx_order_no`(`order_no`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '门票订单表' ROW_FORMAT = Compact;

-- ============================================
-- 游记模块
-- ============================================

-- 游记表
DROP TABLE IF EXISTS `tb_travel_note`;
CREATE TABLE `tb_travel_note` (
  `id` bigint(20) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  `scenic_id` bigint(20) UNSIGNED NULL DEFAULT NULL COMMENT '关联景点id',
  `user_id` bigint(20) UNSIGNED NOT NULL COMMENT '用户id',
  `title` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '标题',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '内容描述',
  `liked` int(8) UNSIGNED NULL DEFAULT 0 COMMENT '点赞数量',
  `comments` int(8) UNSIGNED NULL DEFAULT 0 COMMENT '评论数量',
  `tags` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '标签',
  `is_top` tinyint(1) UNSIGNED NULL DEFAULT 0 COMMENT '是否置顶',
  `status` tinyint(1) UNSIGNED NULL DEFAULT 1 COMMENT '状态，0：草稿，1：发布，2：审核中，3：审核失败，4：删除',
  `create_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_user_id`(`user_id`) USING BTREE,
  INDEX `idx_scenic_id`(`scenic_id`) USING BTREE,
  INDEX `idx_status`(`status`) USING BTREE,
  INDEX `idx_create_time`(`create_time`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '游记表' ROW_FORMAT = Compact;

-- 评论表
DROP TABLE IF EXISTS `tb_note_comment`;
CREATE TABLE `tb_note_comment` (
  `id` bigint(20) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  `user_id` bigint(20) UNSIGNED NOT NULL COMMENT '用户id',
  `note_id` bigint(20) UNSIGNED NOT NULL COMMENT '游记id',
  `parent_id` bigint(20) UNSIGNED NULL DEFAULT 0 COMMENT '父评论id，0为一级评论',
  `answer_id` bigint(20) UNSIGNED NULL DEFAULT 0 COMMENT '回复的评论id',
  `content` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '评论内容',
  `liked` int(8) UNSIGNED NULL DEFAULT 0 COMMENT '点赞数量',
  `status` tinyint(1) UNSIGNED NULL DEFAULT 0 COMMENT '状态，0：正常，1：被举报，2：禁止查看',
  `create_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_note_id`(`note_id`) USING BTREE,
  INDEX `idx_parent_id`(`parent_id`) USING BTREE,
  INDEX `idx_user_id`(`user_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '评论表' ROW_FORMAT = Compact;

-- 游记图片表
DROP TABLE IF EXISTS `tb_note_image`;
CREATE TABLE `tb_note_image` (
  `id` bigint(20) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  `note_id` bigint(20) UNSIGNED NOT NULL COMMENT '游记id',
  `image_url` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '图片URL',
  `sort` int(4) UNSIGNED NULL DEFAULT 0 COMMENT '排序',
  `create_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_note_id`(`note_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '游记图片表' ROW_FORMAT = Compact;

-- ============================================
-- 社交模块
-- ============================================

-- 关注表
DROP TABLE IF EXISTS `tb_follow`;
CREATE TABLE `tb_follow` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `user_id` bigint(20) UNSIGNED NOT NULL COMMENT '用户id',
  `follow_user_id` bigint(20) UNSIGNED NOT NULL COMMENT '关注的用户id',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_user_follow`(`user_id`, `follow_user_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '关注表' ROW_FORMAT = Compact;

-- 收藏表
DROP TABLE IF EXISTS `tb_collect`;
CREATE TABLE `tb_collect` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `user_id` bigint(20) UNSIGNED NOT NULL COMMENT '用户id',
  `target_id` bigint(20) UNSIGNED NOT NULL COMMENT '收藏目标id',
  `target_type` tinyint(1) UNSIGNED NOT NULL COMMENT '目标类型，1：景点，2：游记，3：路线',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_user_target`(`user_id`, `target_id`, `target_type`) USING BTREE,
  INDEX `idx_target_id`(`target_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '收藏表' ROW_FORMAT = Compact;

-- ============================================
-- 路线模块
-- ============================================

-- 旅游路线表
DROP TABLE IF EXISTS `tb_travel_route`;
CREATE TABLE `tb_travel_route` (
  `id` bigint(20) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  `user_id` bigint(20) UNSIGNED NULL DEFAULT NULL COMMENT '创建者id（0为系统推荐）',
  `title` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '路线标题',
  `city` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '目的地城市',
  `days` int(3) UNSIGNED NULL DEFAULT 1 COMMENT '游玩天数',
  `budget` bigint(10) UNSIGNED NULL DEFAULT 0 COMMENT '预算（分）',
  `difficulty` tinyint(1) UNSIGNED NULL DEFAULT 1 COMMENT '难度，1：轻松，2：适中，3：困难',
  `cover_image` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '封面图',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '路线描述',
  `tags` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '标签',
  `view_count` int(8) UNSIGNED NULL DEFAULT 0 COMMENT '浏览次数',
  `collect_count` int(8) UNSIGNED NULL DEFAULT 0 COMMENT '收藏次数',
  `status` tinyint(1) UNSIGNED NULL DEFAULT 1 COMMENT '状态，0：下架，1：上架',
  `is_hot` tinyint(1) UNSIGNED NULL DEFAULT 0 COMMENT '是否热门推荐',
  `create_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_city`(`city`) USING BTREE,
  INDEX `idx_days`(`days`) USING BTREE,
  INDEX `idx_status`(`status`) USING BTREE,
  INDEX `idx_is_hot`(`is_hot`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '旅游路线表' ROW_FORMAT = Compact;

-- 路线详情表
DROP TABLE IF EXISTS `tb_route_detail`;
CREATE TABLE `tb_route_detail` (
  `id` bigint(20) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  `route_id` bigint(20) UNSIGNED NOT NULL COMMENT '路线id',
  `day` int(3) UNSIGNED NOT NULL COMMENT '第几天',
  `scenic_id` bigint(20) UNSIGNED NOT NULL COMMENT '景点id',
  `sort` int(4) UNSIGNED NULL DEFAULT 0 COMMENT '当天排序',
  `description` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '游玩说明',
  `create_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_route_id`(`route_id`) USING BTREE,
  INDEX `idx_route_day`(`route_id`, `day`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '路线详情表' ROW_FORMAT = Compact;

-- ============================================
-- 旅行轨迹模块
-- ============================================

-- 旅行轨迹表
DROP TABLE IF EXISTS `tb_trajectory`;
CREATE TABLE `tb_trajectory` (
  `id` bigint(20) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  `user_id` bigint(20) UNSIGNED NOT NULL COMMENT '用户id',
  `scenic_id` bigint(20) UNSIGNED NOT NULL COMMENT '景点id',
  `visit_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '到访时间',
  `create_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_user_id`(`user_id`) USING BTREE,
  UNIQUE INDEX `uk_user_scenic`(`user_id`, `scenic_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '旅行轨迹表' ROW_FORMAT = Compact;

-- ============================================
-- 插入初始数据
-- ============================================

-- 景点分类数据
INSERT INTO `tb_scenic_type` (`id`, `name`, `icon`, `sort`) VALUES
(1, '自然景观', '/types/zrjg.png', 1),
(2, '人文景观', '/types/rwjg.png', 2),
(3, '主题乐园', '/types/ztyl.png', 3),
(4, '民宿酒店', '/types/msjd.png', 4),
(5, '特色美食', '/types/tsms.png', 5),
(6, '历史古迹', '/types/lsgj.png', 6),
(7, '海滨度假', '/types/hbdj.png', 7),
(8, '山岳森林', '/types/sy森林.png', 8);



SET FOREIGN_KEY_CHECKS = 1;