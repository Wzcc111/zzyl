package com.zzyl.nursing.vo;

import com.zzyl.nursing.domain.Contract;
import io.swagger.annotations.ApiModel;
import lombok.Data;

import java.util.List;

@Data
@ApiModel("入住详情")
public class CheckInDetailVo {
    private CheckInElderVo checkInElderVo;
    private List<ElderFamilyVo> elderFamilyVoList;
    private CheckInConfigVo checkInConfigVo;
    private Contract contract;
}
