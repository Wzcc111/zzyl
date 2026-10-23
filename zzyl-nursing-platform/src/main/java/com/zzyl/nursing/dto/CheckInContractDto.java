package com.zzyl.nursing.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@ApiModel("申请入住合同信息")
public class CheckInContractDto {
    private String agreementPath;
    private String contractName;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime signDate;
    private String thirdPartyName;
    private String thirdPartyPhone;
}
