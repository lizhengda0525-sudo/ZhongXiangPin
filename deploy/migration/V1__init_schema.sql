-- 众享拼 P0 基础功能初始化表结构
-- 约定：
-- 1. 金额统一使用分，字段后缀为 _cent。
-- 2. 业务状态使用 varchar，取值与 docs/plan/openapi.yaml 枚举保持一致。
-- 3. 所有交易事实以 MySQL 为准，Redis 只保存运行态和缓存。
-- 4. JSON 字段保存结构化快照或 payload，不保存手写拼接字符串。

CREATE TABLE user_account (
  id BIGINT NOT NULL AUTO_INCREMENT COMMENT '自增主键',
  username VARCHAR(64) NOT NULL COMMENT '用户名，登录标识之一',
  phone VARCHAR(20) NULL COMMENT '手机号，登录标识之一',
  password_hash VARCHAR(128) NULL COMMENT 'bcrypt 密码摘要，验证码登录轻量账号可为空',
  role VARCHAR(16) NOT NULL DEFAULT 'USER' COMMENT '角色：USER/ADMIN',
  status VARCHAR(16) NOT NULL DEFAULT 'ENABLED' COMMENT '账号状态：ENABLED/DISABLED',
  created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
  updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
  PRIMARY KEY (id),
  UNIQUE KEY uk_user_account_username (username),
  UNIQUE KEY uk_user_account_phone (phone),
  KEY idx_user_account_role_status (role, status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户账号表';

CREATE TABLE sku (
  id BIGINT NOT NULL AUTO_INCREMENT COMMENT '自增主键',
  sku_id BIGINT NOT NULL COMMENT '商品业务 ID',
  name VARCHAR(128) NOT NULL COMMENT '商品名称',
  image_url VARCHAR(512) NULL COMMENT '商品图片地址',
  category VARCHAR(64) NULL COMMENT '商品分类',
  original_price_cent BIGINT NOT NULL COMMENT '商品原价，单位分',
  status VARCHAR(16) NOT NULL DEFAULT 'ENABLED' COMMENT '商品状态：ENABLED/DISABLED',
  created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
  updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
  PRIMARY KEY (id),
  UNIQUE KEY uk_sku_sku_id (sku_id),
  KEY idx_sku_category_status (category, status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商品表';

CREATE TABLE activity (
  id BIGINT NOT NULL AUTO_INCREMENT COMMENT '自增主键',
  activity_id BIGINT NOT NULL COMMENT '活动业务 ID',
  title VARCHAR(128) NOT NULL COMMENT '活动标题',
  source VARCHAR(32) NOT NULL COMMENT '流量来源，如 APP',
  channel VARCHAR(32) NOT NULL COMMENT '渠道，如 H5',
  target_count INT NOT NULL COMMENT '成团人数',
  valid_minutes INT NOT NULL COMMENT '开团后有效分钟数',
  take_limit_count INT NOT NULL DEFAULT 1 COMMENT '单用户限购次数',
  start_time DATETIME(3) NOT NULL COMMENT '活动开始时间',
  end_time DATETIME(3) NOT NULL COMMENT '活动结束时间',
  status VARCHAR(16) NOT NULL DEFAULT 'DRAFT' COMMENT '活动状态：DRAFT/ACTIVE/PAUSED/ENDED',
  tag_id VARCHAR(64) NULL COMMENT '限定人群标签 ID，空表示不限',
  tag_scope VARCHAR(32) NULL COMMENT '标签适用范围，如 USER_ONLY',
  version INT NOT NULL DEFAULT 1 COMMENT '活动版本，用于缓存失效和订单快照',
  created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
  updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
  PRIMARY KEY (id),
  UNIQUE KEY uk_activity_activity_id (activity_id),
  KEY idx_activity_channel_status_time (source, channel, status, start_time, end_time),
  KEY idx_activity_tag_id (tag_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='拼团活动表';

CREATE TABLE activity_sku (
  id BIGINT NOT NULL AUTO_INCREMENT COMMENT '自增主键',
  source VARCHAR(32) NOT NULL COMMENT '流量来源，如 APP',
  channel VARCHAR(32) NOT NULL COMMENT '渠道，如 H5',
  activity_id BIGINT NOT NULL COMMENT '活动业务 ID',
  sku_id BIGINT NOT NULL COMMENT '商品业务 ID',
  status VARCHAR(16) NOT NULL DEFAULT 'ENABLED' COMMENT '绑定状态：ENABLED/DISABLED',
  created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
  updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
  PRIMARY KEY (id),
  UNIQUE KEY uk_activity_sku_entry (source, channel, sku_id),
  KEY idx_activity_sku_activity_status (activity_id, status),
  KEY idx_activity_sku_sku_status (sku_id, status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='活动商品绑定表';

CREATE TABLE discount (
  id BIGINT NOT NULL AUTO_INCREMENT COMMENT '自增主键',
  discount_id BIGINT NOT NULL COMMENT '折扣业务 ID',
  activity_id BIGINT NOT NULL COMMENT '活动业务 ID',
  discount_type VARCHAR(32) NOT NULL COMMENT '折扣类型，如 DIRECT',
  discount_value_cent BIGINT NOT NULL DEFAULT 0 COMMENT '优惠金额，单位分',
  rule_expr JSON NULL COMMENT '折扣规则表达式，结构化 JSON',
  status VARCHAR(16) NOT NULL DEFAULT 'ENABLED' COMMENT '折扣状态：ENABLED/DISABLED',
  created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
  updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
  PRIMARY KEY (id),
  UNIQUE KEY uk_discount_discount_id (discount_id),
  UNIQUE KEY uk_discount_activity (activity_id),
  KEY idx_discount_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='活动折扣表';

CREATE TABLE crowd_tag (
  id BIGINT NOT NULL AUTO_INCREMENT COMMENT '自增主键',
  tag_id VARCHAR(64) NOT NULL COMMENT '标签业务 ID',
  name VARCHAR(128) NOT NULL COMMENT '标签名称',
  tag_desc VARCHAR(256) NULL COMMENT '标签描述',
  current_batch_id VARCHAR(64) NULL COMMENT '当前生效批次 ID',
  statistics JSON NULL COMMENT '当前批次统计信息',
  created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
  updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
  PRIMARY KEY (id),
  UNIQUE KEY uk_crowd_tag_tag_id (tag_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='人群标签表';

CREATE TABLE crowd_tag_job (
  id BIGINT NOT NULL AUTO_INCREMENT COMMENT '自增主键',
  tag_job_id VARCHAR(64) NOT NULL COMMENT '标签任务业务 ID',
  tag_id VARCHAR(64) NOT NULL COMMENT '标签业务 ID',
  tag_name VARCHAR(128) NOT NULL COMMENT '标签名称快照',
  rule_type VARCHAR(32) NOT NULL COMMENT '规则类型：USERS/PARTICIPATE_COUNT',
  rule_expr JSON NOT NULL COMMENT '规则表达式，结构化 JSON',
  status VARCHAR(16) NOT NULL DEFAULT 'INIT' COMMENT '任务状态：INIT/PROCESSING/SUCCESS/FAILED/RETRY_WAIT',
  batch_id VARCHAR(64) NOT NULL COMMENT '本次执行批次 ID',
  stat_start_time DATETIME(3) NULL COMMENT '统计窗口开始时间',
  stat_end_time DATETIME(3) NULL COMMENT '统计窗口结束时间',
  execute_time DATETIME(3) NULL COMMENT '最近执行时间',
  hit_count INT NOT NULL DEFAULT 0 COMMENT '命中人数',
  statistics JSON NULL COMMENT '本次执行统计信息',
  last_error VARCHAR(512) NULL COMMENT '最近失败原因',
  created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
  updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
  PRIMARY KEY (id),
  UNIQUE KEY uk_crowd_tag_job_id (tag_job_id),
  UNIQUE KEY uk_crowd_tag_job_batch (tag_id, batch_id),
  KEY idx_crowd_tag_job_tag_status (tag_id, status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='人群标签任务表';

CREATE TABLE crowd_tag_detail (
  id BIGINT NOT NULL AUTO_INCREMENT COMMENT '自增主键',
  tag_id VARCHAR(64) NOT NULL COMMENT '标签业务 ID',
  batch_id VARCHAR(64) NOT NULL COMMENT '批次 ID',
  user_account_id BIGINT NOT NULL COMMENT '用户账号 ID',
  created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
  PRIMARY KEY (id),
  UNIQUE KEY uk_crowd_tag_detail_user (tag_id, batch_id, user_account_id),
  KEY idx_crowd_tag_detail_user (user_account_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='人群标签明细表';

CREATE TABLE team (
  id BIGINT NOT NULL AUTO_INCREMENT COMMENT '自增主键',
  team_id VARCHAR(64) NOT NULL COMMENT '队伍业务 ID',
  activity_id BIGINT NOT NULL COMMENT '活动业务 ID',
  sku_id BIGINT NOT NULL COMMENT '商品业务 ID',
  source VARCHAR(32) NOT NULL COMMENT '流量来源快照，如 APP',
  channel VARCHAR(32) NOT NULL COMMENT '渠道快照，如 H5',
  target_count INT NOT NULL COMMENT '成团人数',
  complete_count INT NOT NULL DEFAULT 0 COMMENT '已支付人数',
  lock_count INT NOT NULL DEFAULT 0 COMMENT '已锁单未支付人数',
  refund_count INT NOT NULL DEFAULT 0 COMMENT '已退款人数',
  status VARCHAR(32) NOT NULL DEFAULT 'PROGRESS' COMMENT '队伍状态：PROGRESS/COMPLETE/EXPIRED_UNFORMED/COMPLETE_AFTER_REFUND',
  valid_start_time DATETIME(3) NOT NULL COMMENT '队伍有效开始时间',
  valid_end_time DATETIME(3) NOT NULL COMMENT '队伍有效结束时间',
  created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
  updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
  PRIMARY KEY (id),
  UNIQUE KEY uk_team_team_id (team_id),
  KEY idx_team_activity_channel_status_time (activity_id, source, channel, status, valid_end_time),
  KEY idx_team_sku_id (sku_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='拼团队伍表';

CREATE TABLE trade_order (
  id BIGINT NOT NULL AUTO_INCREMENT COMMENT '自增主键',
  order_id VARCHAR(64) NOT NULL COMMENT '订单业务 ID',
  client_order_no VARCHAR(64) NOT NULL COMMENT '客户端幂等号',
  user_account_id BIGINT NOT NULL COMMENT '下单用户账号 ID',
  team_id VARCHAR(64) NOT NULL COMMENT '队伍业务 ID',
  activity_id BIGINT NOT NULL COMMENT '活动业务 ID',
  sku_id BIGINT NOT NULL COMMENT '商品业务 ID',
  source VARCHAR(32) NOT NULL COMMENT '流量来源快照，如 APP',
  channel VARCHAR(32) NOT NULL COMMENT '渠道快照，如 H5',
  occupy_no INT NOT NULL COMMENT '队伍占位序号',
  order_status VARCHAR(32) NOT NULL DEFAULT 'WAIT_PAY' COMMENT '订单状态：WAIT_PAY/PAID/REFUNDED/CLOSED_TIMEOUT',
  original_price_cent BIGINT NOT NULL COMMENT '原价快照，单位分',
  discount_cent BIGINT NOT NULL DEFAULT 0 COMMENT '优惠金额快照，单位分',
  pay_price_cent BIGINT NOT NULL COMMENT '实付金额快照，单位分',
  activity_version INT NOT NULL COMMENT '活动版本快照',
  activity_snapshot JSON NOT NULL COMMENT '活动快照 JSON',
  discount_snapshot JSON NOT NULL COMMENT '折扣快照 JSON',
  trial_no VARCHAR(64) NULL COMMENT '试算流水号',
  trial_time DATETIME(3) NOT NULL COMMENT '试算时间',
  pay_deadline DATETIME(3) NOT NULL COMMENT '支付截止时间',
  paid_at DATETIME(3) NULL COMMENT '支付完成时间',
  closed_at DATETIME(3) NULL COMMENT '超时关闭时间',
  refunded_at DATETIME(3) NULL COMMENT '退款完成时间',
  created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
  updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
  PRIMARY KEY (id),
  UNIQUE KEY uk_trade_order_order_id (order_id),
  UNIQUE KEY uk_trade_order_client (user_account_id, client_order_no),
  UNIQUE KEY uk_trade_order_team_occupy (team_id, occupy_no),
  KEY idx_trade_order_user_status (user_account_id, order_status, created_at),
  KEY idx_trade_order_limit (user_account_id, activity_id, sku_id, order_status),
  KEY idx_trade_order_status_created (order_status, created_at),
  KEY idx_trade_order_team_status (team_id, order_status),
  KEY idx_trade_order_pay_deadline (order_status, pay_deadline)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='交易订单表';

CREATE TABLE pay_record (
  id BIGINT NOT NULL AUTO_INCREMENT COMMENT '自增主键',
  pay_no VARCHAR(64) NOT NULL COMMENT '支付幂等号',
  order_id VARCHAR(64) NOT NULL COMMENT '订单业务 ID',
  user_account_id BIGINT NOT NULL COMMENT '支付用户账号 ID',
  amount_cent BIGINT NOT NULL COMMENT '支付金额，单位分',
  status VARCHAR(16) NOT NULL DEFAULT 'SUCCESS' COMMENT '支付状态：SUCCESS/FAILED',
  paid_at DATETIME(3) NOT NULL COMMENT '支付时间',
  created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
  PRIMARY KEY (id),
  UNIQUE KEY uk_pay_record_pay_no (pay_no),
  KEY idx_pay_record_order_id (order_id),
  KEY idx_pay_record_user_id (user_account_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='支付记录表';

CREATE TABLE refund_record (
  id BIGINT NOT NULL AUTO_INCREMENT COMMENT '自增主键',
  refund_no VARCHAR(64) NOT NULL COMMENT '退款幂等号',
  order_id VARCHAR(64) NOT NULL COMMENT '订单业务 ID',
  user_account_id BIGINT NOT NULL COMMENT '订单用户账号 ID',
  amount_cent BIGINT NOT NULL COMMENT '退款金额，单位分',
  refund_source VARCHAR(32) NOT NULL COMMENT '退款来源：USER/ADMIN/AUTO_EXPIRED',
  reason VARCHAR(256) NOT NULL COMMENT '退款原因',
  operator_id BIGINT NULL COMMENT '管理员操作者 ID，用户或自动退款为空',
  status VARCHAR(16) NOT NULL DEFAULT 'SUCCESS' COMMENT '退款状态：SUCCESS/FAILED',
  refunded_at DATETIME(3) NOT NULL COMMENT '退款时间',
  created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
  PRIMARY KEY (id),
  UNIQUE KEY uk_refund_record_refund_no (refund_no),
  KEY idx_refund_record_order_id (order_id),
  KEY idx_refund_record_user_id (user_account_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='退款记录表';

CREATE TABLE reliable_event (
  id BIGINT NOT NULL AUTO_INCREMENT COMMENT '自增主键',
  event_id VARCHAR(64) NOT NULL COMMENT '可靠事件业务 ID',
  event_type VARCHAR(64) NOT NULL COMMENT '事件类型：TEAM_COMPLETE_NOTIFY/REDIS_SLOT_RELEASE/TEAM_REBUILD/ORDER_TIMEOUT_REPAIR/REFUND_REPAIR',
  biz_key VARCHAR(128) NOT NULL COMMENT '业务唯一键，如 teamId 或 orderId',
  status VARCHAR(16) NOT NULL DEFAULT 'INIT' COMMENT '事件状态：INIT/PROCESSING/SUCCESS/FAILED/RETRY_WAIT',
  retry_count INT NOT NULL DEFAULT 0 COMMENT '已重试次数',
  max_retry_count INT NOT NULL DEFAULT 5 COMMENT '最大重试次数',
  next_execute_time DATETIME(3) NOT NULL COMMENT '下次执行时间',
  last_execute_time DATETIME(3) NULL COMMENT '最近执行时间',
  payload JSON NOT NULL COMMENT '事件载荷 JSON',
  last_error VARCHAR(1024) NULL COMMENT '最近失败原因',
  locked_by VARCHAR(64) NULL COMMENT '执行锁持有者',
  locked_until DATETIME(3) NULL COMMENT '执行锁过期时间',
  created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
  updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
  PRIMARY KEY (id),
  UNIQUE KEY uk_reliable_event_id (event_id),
  UNIQUE KEY uk_reliable_event_biz (event_type, biz_key),
  KEY idx_reliable_event_due (status, next_execute_time),
  KEY idx_reliable_event_type_status_created (event_type, status, created_at),
  KEY idx_reliable_event_biz_key (biz_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='可靠事件表';

CREATE TABLE dcc_config (
  id BIGINT NOT NULL AUTO_INCREMENT COMMENT '自增主键',
  config_key VARCHAR(128) NOT NULL COMMENT '配置键',
  config_value VARCHAR(512) NOT NULL COMMENT '配置值',
  description VARCHAR(256) NULL COMMENT '配置说明',
  updated_by BIGINT NULL COMMENT '最近更新管理员 ID',
  created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
  updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
  PRIMARY KEY (id),
  UNIQUE KEY uk_dcc_config_key (config_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='动态配置表';

CREATE TABLE admin_operation_log (
  id BIGINT NOT NULL AUTO_INCREMENT COMMENT '自增主键',
  audit_id VARCHAR(64) NOT NULL COMMENT '审计业务 ID',
  operator_id BIGINT NOT NULL COMMENT '管理员用户 ID',
  operator_name VARCHAR(64) NOT NULL COMMENT '管理员用户名快照',
  action VARCHAR(64) NOT NULL COMMENT '操作动作',
  target_type VARCHAR(64) NOT NULL COMMENT '业务对象类型，如 ORDER/ACTIVITY/EVENT',
  target_id VARCHAR(128) NOT NULL COMMENT '业务对象 ID',
  before_data JSON NULL COMMENT '变更前数据 JSON',
  after_data JSON NULL COMMENT '变更后数据 JSON',
  result VARCHAR(16) NOT NULL COMMENT '操作结果：SUCCESS/FAILED',
  reason VARCHAR(256) NULL COMMENT '操作原因',
  trace_id VARCHAR(64) NOT NULL COMMENT '请求追踪 ID',
  created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
  PRIMARY KEY (id),
  UNIQUE KEY uk_admin_operation_log_audit_id (audit_id),
  KEY idx_admin_operation_log_operator (operator_id, created_at),
  KEY idx_admin_operation_log_target (target_type, target_id, created_at),
  KEY idx_admin_operation_log_trace_id (trace_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='管理员操作审计日志表';

INSERT INTO user_account (
  id, username, phone, password_hash, role, status
) VALUES
  (1, 'admin_demo', '13800000001', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'ADMIN', 'ENABLED'),
  (2, 'user_demo', '13800000002', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'USER', 'ENABLED');

INSERT INTO sku (
  sku_id, name, image_url, category, original_price_cent, status
) VALUES
  (10001, '有机苹果礼盒', 'https://example.com/static/sku/apple-box.png', '生鲜水果', 12900, 'ENABLED'),
  (10002, '手工曲奇组合', 'https://example.com/static/sku/cookie-box.png', '休闲零食', 8900, 'ENABLED'),
  (10003, '家用清洁套装', 'https://example.com/static/sku/clean-set.png', '家居日用', 15900, 'ENABLED');

INSERT INTO crowd_tag (
  tag_id, name, tag_desc, current_batch_id, statistics
) VALUES
  ('TAG-VIP-001', '高价值用户', '近 30 天有拼团参与记录的演示人群', 'BATCH-VIP-202605', JSON_OBJECT('hitCount', 1));

INSERT INTO activity (
  activity_id, title, source, channel, target_count, valid_minutes, take_limit_count,
  start_time, end_time, status, tag_id, tag_scope, version
) VALUES
  (20001, '水果尝鲜拼团', 'APP', 'H5', 3, 30, 2, '2026-05-01 00:00:00.000', '2026-12-31 23:59:59.000', 'ACTIVE', NULL, NULL, 1),
  (20002, 'VIP 零食专享团', 'APP', 'H5', 2, 45, 1, '2026-05-01 00:00:00.000', '2026-12-31 23:59:59.000', 'ACTIVE', 'TAG-VIP-001', 'USER_ONLY', 1);

INSERT INTO activity_sku (
  source, channel, activity_id, sku_id, status
) VALUES
  ('APP', 'H5', 20001, 10001, 'ENABLED'),
  ('APP', 'H5', 20002, 10002, 'ENABLED'),
  ('APP', 'MINI_PROGRAM', 20001, 10003, 'ENABLED');

INSERT INTO discount (
  discount_id, activity_id, discount_type, discount_value_cent, rule_expr, status
) VALUES
  (30001, 20001, 'DIRECT', 3000, JSON_OBJECT('type', 'DIRECT', 'discountCent', 3000), 'ENABLED'),
  (30002, 20002, 'GROUP_PRICE', 0, JSON_OBJECT('type', 'GROUP_PRICE', 'groupPriceCent', 5900), 'ENABLED');

INSERT INTO crowd_tag_job (
  tag_job_id, tag_id, tag_name, rule_type, rule_expr, status, batch_id,
  stat_start_time, stat_end_time, execute_time, hit_count, statistics
) VALUES
  (
    'TAG-JOB-VIP-001',
    'TAG-VIP-001',
    '高价值用户',
    'PARTICIPATE_COUNT',
    JSON_OBJECT('tagRule', 'participateCount>=1', 'threshold', 1),
    'SUCCESS',
    'BATCH-VIP-202605',
    '2026-05-01 00:00:00.000',
    '2026-05-31 23:59:59.000',
    '2026-05-31 10:00:00.000',
    1,
    JSON_OBJECT('hitCount', 1)
  );

INSERT INTO crowd_tag_detail (
  tag_id, batch_id, user_account_id
) VALUES
  ('TAG-VIP-001', 'BATCH-VIP-202605', 2);

INSERT INTO dcc_config (
  config_key, config_value, description, updated_by
) VALUES
  ('trade.lock.pay-timeout-minutes', '15', '锁单后支付等待分钟数', 1),
  ('runtime.event.max-retry-count', '5', '可靠事件最大重试次数', 1);

INSERT INTO admin_operation_log (
  audit_id, operator_id, operator_name, action, target_type, target_id,
  before_data, after_data, result, reason, trace_id
) VALUES
  (
    'AUDIT-SEED-001',
    1,
    'admin_demo',
    'INIT_SEED',
    'MIGRATION',
    'V1__init_schema',
    NULL,
    JSON_OBJECT('seed', true),
    'SUCCESS',
    '初始化 M2 验收种子数据',
    'trace-seed-m2-001'
  );
