-- 第5章“护理计划功能开发”使用的状态字典，可重复执行。
START TRANSACTION;

INSERT INTO sys_dict_type
    (dict_name, dict_type, status, create_by, create_time, remark)
SELECT '护理计划状态', 'nursing_plan_status', '0', 'admin', NOW(), '护理计划启用禁用状态'
WHERE NOT EXISTS (
    SELECT 1 FROM sys_dict_type WHERE dict_type = 'nursing_plan_status'
);

INSERT INTO sys_dict_data
    (dict_sort, dict_label, dict_value, dict_type, css_class, list_class,
     is_default, status, create_by, create_time, remark)
SELECT 1, '启用', '1', 'nursing_plan_status', '', 'success',
       'Y', '0', 'admin', NOW(), '护理计划启用状态'
WHERE NOT EXISTS (
    SELECT 1 FROM sys_dict_data
    WHERE dict_type = 'nursing_plan_status' AND dict_value = '1'
);

INSERT INTO sys_dict_data
    (dict_sort, dict_label, dict_value, dict_type, css_class, list_class,
     is_default, status, create_by, create_time, remark)
SELECT 2, '禁用', '0', 'nursing_plan_status', '', 'danger',
       'N', '0', 'admin', NOW(), '护理计划禁用状态'
WHERE NOT EXISTS (
    SELECT 1 FROM sys_dict_data
    WHERE dict_type = 'nursing_plan_status' AND dict_value = '0'
);

COMMIT;
