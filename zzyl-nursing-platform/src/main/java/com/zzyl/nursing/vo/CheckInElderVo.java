package com.zzyl.nursing.vo;

import io.swagger.annotations.ApiModel;
import lombok.Data;

@Data
@ApiModel("入住详情老人信息")
public class CheckInElderVo {
    private Long id;
    private String name;
    private String idCardNo;
    private String birthday;
    private Integer sex;
    private String phone;
    private String address;
    private String image;
    private String idCardNationalEmblemImg;
    private String idCardPortraitImg;
    private Integer age;
}
