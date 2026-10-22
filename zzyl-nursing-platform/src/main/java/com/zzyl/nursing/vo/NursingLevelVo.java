package com.zzyl.nursing.vo;

import java.math.BigDecimal;
import java.util.Date;
import lombok.Data;

/** 护理等级列表项，附带关联的护理计划名称。 */
@Data
public class NursingLevelVo {
    private Long id;
    private String name;
    private Long planId;
    private String planName;
    private BigDecimal fee;
    private Integer status;
    private String description;
    private Date createTime;
}
