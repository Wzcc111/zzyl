package com.zzyl.nursing.dto;

import com.zzyl.nursing.domain.NursingProjectPlan;
import lombok.Data;
import java.util.List;

/** 新增和修改护理计划的请求。 */
@Data
public class NursingPlanDto {
    private Long id;
    private Integer sortNo;
    private String planName;
    private Integer status;
    private String remark;
    private List<NursingProjectPlan> projectPlans;
}
