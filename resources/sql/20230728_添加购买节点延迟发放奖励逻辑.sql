-- ---------------------------
-- Table structure for buy_node_delay_amount
-- ---------------------------

DROP TABLE IF EXISTS `buy_node_delay_amount`;
CREATE TABLE `buy_node_delay_amount`
(
    `id`               int(0) UNSIGNED NOT NULL AUTO_INCREMENT,
    `userId`           int(0) UNSIGNED NOT NULL COMMENT '购买节点的用户Id',
    `type`             tinyint(2) NOT NULL DEFAULT 2 COMMENT '类型 1=购买节点奖励 2=推广购买节点奖励',
    `day`              tinyint UNSIGNED NOT NULL DEFAULT 0 COMMENT '已发放天数',
    `subUserId`        int(0) UNSIGNED  NULL COMMENT '推广的用户id',
    `amount`           int(0) UNSIGNED NOT NULL COMMENT '发放的总金额 每天发放的金额=总金额/200',
    `createTime`       datetime(0) NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `lastmodifiedTime` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP COMMENT '修改时间',
    PRIMARY KEY (`id`) USING BTREE,
    CONSTRAINT `FK_buy_node_delay_amount_userId` FOREIGN KEY (`userId`) REFERENCES `app_users` (`id`)
) ENGINE = InnoDB
  CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT = '购买节点延迟奖励发放'
  ROW_FORMAT = Dynamic;
