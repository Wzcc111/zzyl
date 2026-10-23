package com.zzyl.nursing.vo;

import io.swagger.annotations.ApiModel;
import lombok.Data;

@Data
@ApiModel("入住详情家属信息")
public class ElderFamilyVo {
    private String name;
    private String phone;
    private String kinship;
}
