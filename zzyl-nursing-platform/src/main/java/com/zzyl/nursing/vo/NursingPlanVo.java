package com.zzyl.nursing.vo;

import lombok.Data;
import java.util.List;

/** 带执行项目的护理计划详情。 */
@Data
public class NursingPlanVo {
    private Long id;
    private Integer sortNo;
    private String planName;
    private Integer status;
    private String remark;
    private List<NursingProjectPlanVo> projectPlans;
}
