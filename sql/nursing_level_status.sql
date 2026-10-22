-- 护理等级状态字典，可重复执行。
START TRANSACTION;

INSERT INTO sys_dict_type
    (dict_name, dict_type, status, create_by, create_time, remark)
SELECT '护理等级状态', 'nursing_level_status', '0', 'admin', NOW(), '护理等级启用禁用状态'
WHERE NOT EXISTS (
    SELECT 1 FROM sys_dict_type WHERE dict_type = 'nursing_level_status'
);

INSERT INTO sys_dict_data
    (dict_sort, dict_label, dict_value, dict_type, css_class, list_class,
     is_default, status, create_by, create_time, remark)
SELECT 1, '启用', '1', 'nursing_level_status', '', 'success',
       'Y', '0', 'admin', NOW(), '护理等级启用状态'
WHERE NOT EXISTS (
    SELECT 1 FROM sys_dict_data
    WHERE dict_type = 'nursing_level_status' AND dict_value = '1'
);

INSERT INTO sys_dict_data
    (dict_sort, dict_label, dict_value, dict_type, css_class, list_class,
     is_default, status, create_by, create_time, remark)
SELECT 2, '禁用', '0', 'nursing_level_status', '', 'danger',
       'N', '0', 'admin', NOW(), '护理等级禁用状态'
WHERE NOT EXISTS (
    SELECT 1 FROM sys_dict_data
    WHERE dict_type = 'nursing_level_status' AND dict_value = '0'
);

COMMIT;
