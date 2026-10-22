package com.zzyl.nursing.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zzyl.nursing.domain.NursingProjectPlan;
import com.zzyl.nursing.vo.NursingProjectPlanVo;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface NursingProjectPlanMapper extends BaseMapper<NursingProjectPlan> {
    List<NursingProjectPlanVo> selectByPlanId(@Param("planId") Long planId);

    int insertForPlan(@Param("planId") Long planId, @Param("items") List<NursingProjectPlan> items);

    int deleteByPlanId(@Param("planId") Long planId);
}
