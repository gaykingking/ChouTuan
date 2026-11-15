-- ChouTuan Multi-Tenant & Sharding MySQL Schema (user_id % 2)
-- Zero-Commission Policy Enforced

CREATE TABLE IF NOT EXISTS `t_tenant` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'Primary Key',
  `tenant_id` VARCHAR(64) NOT NULL COMMENT 'Tenant Identifier',
  `app_id` VARCHAR(64) NOT NULL DEFAULT 'com.choutuan.app' COMMENT 'Client App ID',
  `tenant_name` VARCHAR(128) NOT NULL COMMENT 'Merchant / Tenant Name',
  `commission_policy` VARCHAR(32) NOT NULL DEFAULT 'ZERO_COMMISSION' COMMENT 'Commission Policy',
  `commission_rate` DECIMAL(5,4) NOT NULL DEFAULT 0.0000 COMMENT 'Zero Commission Rate',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '1: Active, 0: Disabled',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_tenant_id` (`tenant_id`),
  KEY `idx_app_tenant` (`app_id`, `tenant_id`),
  CONSTRAINT `chk_tenant_zero_commission` CHECK (`commission_rate` = 0.0000 AND `commission_policy` = 'ZERO_COMMISSION')
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Multi-Tenant Merchant Table';

CREATE TABLE IF NOT EXISTS `t_order_0` (
  `order_id` BIGINT UNSIGNED NOT NULL COMMENT 'Distributed Order ID',
  `tenant_id` VARCHAR(64) NOT NULL COMMENT 'Tenant ID',
  `user_id` BIGINT UNSIGNED NOT NULL COMMENT 'Sharding Key (user_id % 2 = 0)',
  `merchant_id` BIGINT UNSIGNED NOT NULL COMMENT 'Merchant ID',
  `deal_id` BIGINT UNSIGNED NOT NULL COMMENT 'Group Deal ID',
  `total_amount` DECIMAL(12,2) NOT NULL COMMENT 'Order Total Amount',
  `platform_commission` DECIMAL(12,2) NOT NULL DEFAULT 0.00 COMMENT 'Zero Platform Commission',
  `commission_policy` VARCHAR(32) NOT NULL DEFAULT 'ZERO_COMMISSION' COMMENT 'Commission Policy',
  `order_status` VARCHAR(32) NOT NULL DEFAULT 'CREATED' COMMENT 'Order Status',
  `trace_id` VARCHAR(64) DEFAULT NULL COMMENT 'Distributed Trace ID',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`order_id`),
  KEY `idx_tenant_user` (`tenant_id`, `user_id`),
  KEY `idx_user_created` (`user_id`, `created_at`),
  CONSTRAINT `chk_order_0_zero_commission` CHECK (`platform_commission` = 0.00 AND `commission_policy` = 'ZERO_COMMISSION')
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Sharded Order Table 0 (user_id % 2 = 0)';

CREATE TABLE IF NOT EXISTS `t_order_1` (
  `order_id` BIGINT UNSIGNED NOT NULL COMMENT 'Distributed Order ID',
  `tenant_id` VARCHAR(64) NOT NULL COMMENT 'Tenant ID',
  `user_id` BIGINT UNSIGNED NOT NULL COMMENT 'Sharding Key (user_id % 2 = 1)',
  `merchant_id` BIGINT UNSIGNED NOT NULL COMMENT 'Merchant ID',
  `deal_id` BIGINT UNSIGNED NOT NULL COMMENT 'Group Deal ID',
  `total_amount` DECIMAL(12,2) NOT NULL COMMENT 'Order Total Amount',
  `platform_commission` DECIMAL(12,2) NOT NULL DEFAULT 0.00 COMMENT 'Zero Platform Commission',
  `commission_policy` VARCHAR(32) NOT NULL DEFAULT 'ZERO_COMMISSION' COMMENT 'Commission Policy',
  `order_status` VARCHAR(32) NOT NULL DEFAULT 'CREATED' COMMENT 'Order Status',
  `trace_id` VARCHAR(64) DEFAULT NULL COMMENT 'Distributed Trace ID',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`order_id`),
  KEY `idx_tenant_user` (`tenant_id`, `user_id`),
  KEY `idx_user_created` (`user_id`, `created_at`),
  CONSTRAINT `chk_order_1_zero_commission` CHECK (`platform_commission` = 0.00 AND `commission_policy` = 'ZERO_COMMISSION')
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Sharded Order Table 1 (user_id % 2 = 1)';

CREATE TABLE IF NOT EXISTS `t_profit_sharing_ledger_0` (
  `ledger_id` BIGINT UNSIGNED NOT NULL COMMENT 'Ledger ID',
  `order_id` BIGINT UNSIGNED NOT NULL COMMENT 'Order ID',
  `tenant_id` VARCHAR(64) NOT NULL COMMENT 'Tenant ID',
  `user_id` BIGINT UNSIGNED NOT NULL COMMENT 'Sharding Key (user_id % 2 = 0)',
  `merchant_id` BIGINT UNSIGNED NOT NULL COMMENT 'Merchant ID',
  `gross_amount` DECIMAL(12,2) NOT NULL COMMENT 'Gross Payment Amount',
  `merchant_receive_amount` DECIMAL(12,2) NOT NULL COMMENT 'Merchant Net Amount (100%)',
  `platform_commission` DECIMAL(12,2) NOT NULL DEFAULT 0.00 COMMENT 'Zero Platform Commission',
  `commission_policy` VARCHAR(32) NOT NULL DEFAULT 'ZERO_COMMISSION' COMMENT 'Commission Policy',
  `settle_status` VARCHAR(32) NOT NULL DEFAULT 'PENDING' COMMENT 'Settlement Status',
  `tx_id` VARCHAR(64) NOT NULL COMMENT 'RocketMQ Half-Tx ID',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`ledger_id`),
  UNIQUE KEY `uk_order_id` (`order_id`),
  KEY `idx_tenant_merchant` (`tenant_id`, `merchant_id`),
  CONSTRAINT `chk_ledger_0_zero_commission` CHECK (`platform_commission` = 0.00 AND `commission_policy` = 'ZERO_COMMISSION')
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Transparent Profit-Sharing Ledger 0';

CREATE TABLE IF NOT EXISTS `t_profit_sharing_ledger_1` (
  `ledger_id` BIGINT UNSIGNED NOT NULL COMMENT 'Ledger ID',
  `order_id` BIGINT UNSIGNED NOT NULL COMMENT 'Order ID',
  `tenant_id` VARCHAR(64) NOT NULL COMMENT 'Tenant ID',
  `user_id` BIGINT UNSIGNED NOT NULL COMMENT 'Sharding Key (user_id % 2 = 1)',
  `merchant_id` BIGINT UNSIGNED NOT NULL COMMENT 'Merchant ID',
  `gross_amount` DECIMAL(12,2) NOT NULL COMMENT 'Gross Payment Amount',
  `merchant_receive_amount` DECIMAL(12,2) NOT NULL COMMENT 'Merchant Net Amount (100%)',
  `platform_commission` DECIMAL(12,2) NOT NULL DEFAULT 0.00 COMMENT 'Zero Platform Commission',
  `commission_policy` VARCHAR(32) NOT NULL DEFAULT 'ZERO_COMMISSION' COMMENT 'Commission Policy',
  `settle_status` VARCHAR(32) NOT NULL DEFAULT 'PENDING' COMMENT 'Settlement Status',
  `tx_id` VARCHAR(64) NOT NULL COMMENT 'RocketMQ Half-Tx ID',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`ledger_id`),
  UNIQUE KEY `uk_order_id` (`order_id`),
  KEY `idx_tenant_merchant` (`tenant_id`, `merchant_id`),
  CONSTRAINT `chk_ledger_1_zero_commission` CHECK (`platform_commission` = 0.00 AND `commission_policy` = 'ZERO_COMMISSION')
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Transparent Profit-Sharing Ledger 1';

CREATE TABLE IF NOT EXISTS `t_group_voucher` (
  `voucher_id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'Voucher ID',
  `voucher_code` VARCHAR(64) NOT NULL COMMENT 'Unique Verification Code',
  `order_id` BIGINT UNSIGNED NOT NULL COMMENT 'Order ID',
  `tenant_id` VARCHAR(64) NOT NULL COMMENT 'Tenant ID',
  `user_id` BIGINT UNSIGNED NOT NULL COMMENT 'User ID',
  `merchant_id` BIGINT UNSIGNED NOT NULL COMMENT 'Merchant ID',
  `face_value` DECIMAL(12,2) NOT NULL COMMENT 'Voucher Face Value',
  `platform_commission` DECIMAL(12,2) NOT NULL DEFAULT 0.00 COMMENT 'Zero Platform Commission',
  `commission_policy` VARCHAR(32) NOT NULL DEFAULT 'ZERO_COMMISSION' COMMENT 'Commission Policy',
  `voucher_status` VARCHAR(32) NOT NULL DEFAULT 'UNUSED' COMMENT 'UNUSED / VERIFIED / EXPIRED',
  `verified_at` DATETIME DEFAULT NULL COMMENT 'In-Store Verification Timestamp',
  `expire_at` DATETIME NOT NULL COMMENT 'Expiration Timestamp',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`voucher_id`),
  UNIQUE KEY `uk_voucher_code` (`voucher_code`),
  KEY `idx_tenant_id` (`tenant_id`),
  KEY `idx_order_id` (`order_id`),
  CONSTRAINT `chk_voucher_zero_commission` CHECK (`platform_commission` = 0.00 AND `commission_policy` = 'ZERO_COMMISSION')
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='In-Store Group Voucher Table';
