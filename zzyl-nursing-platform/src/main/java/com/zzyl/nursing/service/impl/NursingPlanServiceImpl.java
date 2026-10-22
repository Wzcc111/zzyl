package com.zzyl.nursing.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zzyl.nursing.domain.NursingPlan;
import com.zzyl.nursing.domain.NursingProjectPlan;
import com.zzyl.nursing.dto.NursingPlanDto;
import com.zzyl.nursing.mapper.NursingPlanMapper;
import com.zzyl.nursing.mapper.NursingProjectPlanMapper;
import com.zzyl.nursing.service.INursingPlanService;
import com.zzyl.nursing.vo.NursingPlanVo;
import java.util.List;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class NursingPlanServiceImpl extends ServiceImpl<NursingPlanMapper, NursingPlan>
        implements INursingPlanService {
    @Autowired
    private NursingPlanMapper nursingPlanMapper;

    @Autowired
    private NursingProjectPlanMapper projectPlanMapper;

    @Override
    public NursingPlanVo selectNursingPlanById(Long id) {
        NursingPlan plan = nursingPlanMapper.selectById(id);
        if (plan == null) {
            return null;
        }
        NursingPlanVo result = new NursingPlanVo();
        BeanUtils.copyProperties(plan, result);
        result.setProjectPlans(projectPlanMapper.selectByPlanId(id));
        return result;
    }

    @Override
    public List<NursingPlan> selectNursingPlanList(NursingPlan plan) {
        return nursingPlanMapper.selectNursingPlanList(plan);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int insertNursingPlan(NursingPlanDto dto) {
        NursingPlan plan = toPlan(dto);
        int changed = nursingPlanMapper.insert(plan);
        if (changed != 1) {
            return changed;
        }
        insertProjects(plan.getId(), dto.getProjectPlans());
        return changed;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateNursingPlan(NursingPlanDto dto) {
        if (dto.getId() == null) {
            throw new IllegalArgumentException("护理计划ID不能为空");
        }
        int changed = nursingPlanMapper.updateById(toPlan(dto));
        if (changed != 1) {
            return changed;
        }
        // null 表示只改主表；空列表表示明确清空关联项目。
        if (dto.getProjectPlans() != null) {
            projectPlanMapper.deleteByPlanId(dto.getId());
            insertProjects(dto.getId(), dto.getProjectPlans());
        }
        return changed;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteNursingPlanByIds(Long[] ids) {
        if (ids == null || ids.length == 0) {
            return 0;
        }
        int changed = 0;
        for (Long id : ids) {
            projectPlanMapper.deleteByPlanId(id);
            changed += nursingPlanMapper.deleteById(id);
        }
        return changed;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteNursingPlanById(Long id) {
        projectPlanMapper.deleteByPlanId(id);
        return nursingPlanMapper.deleteById(id);
    }

    private NursingPlan toPlan(NursingPlanDto dto) {
        NursingPlan plan = new NursingPlan();
        BeanUtils.copyProperties(dto, plan);
        return plan;
    }

    private void insertProjects(Long planId, List<NursingProjectPlan> items) {
        if (items == null || items.isEmpty()) {
            return;
        }
        for (NursingProjectPlan item : items) {
            if (item == null || item.getProjectId() == null || item.getExecuteTime() == null
                    || item.getExecuteTime().trim().isEmpty() || item.getExecuteCycle() == null
                    || item.getExecuteFrequency() == null) {
                throw new IllegalArgumentException("护理项目及执行安排不能为空");
            }
        }
        int inserted = projectPlanMapper.insertForPlan(planId, items);
        if (inserted != items.size()) {
            throw new IllegalStateException("护理项目保存数量不一致");
        }
    }
}
