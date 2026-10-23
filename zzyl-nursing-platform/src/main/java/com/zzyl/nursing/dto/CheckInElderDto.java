package com.zzyl.nursing.dto;

import io.swagger.annotations.ApiModel;
import lombok.Data;

@Data
@ApiModel("申请入住老人信息")
public class CheckInElderDto {
    private String name;
    private String idCardNo;
    private String birthday;
    private Integer age;
    private Integer sex;
    private String phone;
    private String address;
    private String image;
    private String idCardPortraitImg;
    private String idCardNationalEmblemImg;
}
