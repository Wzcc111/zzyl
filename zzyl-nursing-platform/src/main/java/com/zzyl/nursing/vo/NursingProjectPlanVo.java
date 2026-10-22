package com.zzyl.nursing.vo;

import com.zzyl.common.core.domain.BaseEntity;
import lombok.Data;

/** 护理计划项目详情；项目 ID 使用字符串，便于与下拉选项回显。 */
@Data
public class NursingProjectPlanVo extends BaseEntity {
    private Long id;
    private Long planId;
    private String projectId;
    private String executeTime;
    private String executeCycle;
    private Integer executeFrequency;
}
