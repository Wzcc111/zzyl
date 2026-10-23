package com.zzyl.nursing.dto;

import io.swagger.annotations.ApiModel;
import lombok.Data;

import java.util.List;

@Data
@ApiModel("申请入住参数")
public class CheckInApplyDto {
    private CheckInElderDto checkInElderDto;
    private List<ElderFamilyDto> elderFamilyDtoList;
    private CheckInConfigDto checkInConfigDto;
    private CheckInContractDto checkInContractDto;
}
