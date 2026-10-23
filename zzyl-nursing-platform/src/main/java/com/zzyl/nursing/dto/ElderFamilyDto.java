package com.zzyl.nursing.dto;

import io.swagger.annotations.ApiModel;
import lombok.Data;

@Data
@ApiModel("入住老人家属信息")
public class ElderFamilyDto {
    private String name;
    private String phone;
    private String kinship;
}
