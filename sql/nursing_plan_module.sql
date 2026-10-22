-- 护理计划模块需要的两张表。执行前请先选择项目数据库。
CREATE TABLE IF NOT EXISTS `nursing_plan` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '编号',
  `sort_no` int DEFAULT NULL COMMENT '排序号',
  `plan_name` varchar(255) NOT NULL DEFAULT '' COMMENT '名称',
  `status` tinyint NOT NULL DEFAULT 0 COMMENT '0禁用 1启用',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `create_by` bigint DEFAULT NULL,
  `update_by` bigint DEFAULT NULL,
  `remark` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_nursing_plan_name` (`plan_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='护理计划表';

CREATE TABLE IF NOT EXISTS `nursing_project_plan` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `plan_id` bigint NOT NULL COMMENT '护理计划ID',
  `project_id` bigint NOT NULL COMMENT '护理项目ID',
  `execute_time` varchar(50) NOT NULL COMMENT '计划执行时间',
  `execute_cycle` int NOT NULL COMMENT '0天 1周 2月',
  `execute_frequency` int NOT NULL COMMENT '执行频次',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `create_by` bigint DEFAULT NULL,
  `update_by` bigint DEFAULT NULL,
  `remark` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_nursing_project_plan_plan_id` (`plan_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='护理计划和项目关联表';
