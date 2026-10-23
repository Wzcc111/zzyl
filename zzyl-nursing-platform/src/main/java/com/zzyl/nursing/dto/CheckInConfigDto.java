package com.zzyl.nursing.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@ApiModel("申请入住配置信息")
public class CheckInConfigDto {
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startDate;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endDate;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime feeStartDate;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime feeEndDate;
    private Long nursingLevelId;
    private String nursingLevelName;
    private BigDecimal bedFee;
    private Long bedId;
    private String code;
    private Long floorId;
    private String floorName;
    private BigDecimal governmentSubsidy;
    private BigDecimal insurancePayment;
    private BigDecimal nursingFee;
    private BigDecimal otherFees;
    private Long roomId;
    private BigDecimal deposit;
}
