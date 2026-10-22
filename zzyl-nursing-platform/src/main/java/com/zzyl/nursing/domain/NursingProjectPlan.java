package com.zzyl.nursing.domain;

import com.zzyl.common.core.domain.BaseEntity;
import lombok.Data;

/** 护理计划与护理项目的执行安排。 */
@Data
public class NursingProjectPlan extends BaseEntity {
    private Long id;
    private Long planId;
    private Long projectId;
    private String executeTime;
    private Integer executeCycle;
    private Integer executeFrequency;
}
