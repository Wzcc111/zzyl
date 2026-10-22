package com.zzyl.nursing.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.zzyl.nursing.mapper.NursingLevelMapper;
import com.zzyl.nursing.domain.NursingLevel;
import com.zzyl.nursing.service.INursingLevelService;
import com.zzyl.nursing.vo.NursingLevelVo;
import com.zzyl.nursing.mapper.NursingPlanMapper;
import com.zzyl.common.exception.ServiceException;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zzyl.nursing.domain.NursingPlan;
import java.math.BigDecimal;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import java.util.Arrays;

/**
 * 护理等级Service业务层处理
 *
 * @author wzc
 * @date 2026-10-20
 */
@Service
public class NursingLevelServiceImpl extends ServiceImpl<NursingLevelMapper, NursingLevel> implements INursingLevelService
{
    @Autowired
    private NursingLevelMapper nursingLevelMapper;

    @Autowired
    private NursingPlanMapper nursingPlanMapper;

    /**
     * 查询护理等级
     *
     * @param id 护理等级主键
     * @return 护理等级
     */
    @Override
    public NursingLevel selectNursingLevelById(Long id)
    {
                return getById(id);
    }

    /**
     * 查询护理等级列表
     *
     * @param nursingLevel 护理等级
     * @return 护理等级
     */
    @Override
    public List<NursingLevel> selectNursingLevelList(NursingLevel nursingLevel)
    {
        return nursingLevelMapper.selectNursingLevelList(nursingLevel);
    }

    @Override
    public List<NursingLevelVo> selectNursingLevelVoList(NursingLevel nursingLevel) {
        return nursingLevelMapper.selectNursingLevelVoList(nursingLevel);
    }

    @Override
    public List<NursingLevel> selectAllNursingLevels() {
        return nursingLevelMapper.selectList(null);
    }

    /**
     * 新增护理等级
     *
     * @param nursingLevel 护理等级
     * @return 结果
     */
    @Override
    public int insertNursingLevel(NursingLevel nursingLevel)
    {
        validateLevel(nursingLevel, true);
        return nursingLevelMapper.insert(nursingLevel);
    }

    /**
     * 修改护理等级
     *
     * @param nursingLevel 护理等级
     * @return 结果
     */
    @Override
    public int updateNursingLevel(NursingLevel nursingLevel)
    {
        if (nursingLevel == null || nursingLevel.getId() == null) {
            throw new ServiceException("护理等级ID不能为空");
        }
        validateLevel(nursingLevel, false);
        return nursingLevelMapper.updateById(nursingLevel);
    }

    /**
     * 批量删除护理等级
     *
     * @param ids 需要删除的护理等级主键
     * @return 结果
     */
    @Override
    public int deleteNursingLevelByIds(Long[] ids)
    {
                return removeByIds(Arrays.asList(ids)) ? 1 : 0;
    }

    /**
     * 删除护理等级信息
     *
     * @param id 护理等级主键
     * @return 结果
     */
    @Override
    public int deleteNursingLevelById(Long id)
    {
                return removeById(id) ? 1 : 0;
    }

    private void validateLevel(NursingLevel level, boolean creating) {
        if (level == null) {
            throw new ServiceException("护理等级不能为空");
        }
        if (creating && (level.getName() == null || level.getName().trim().isEmpty()
                || level.getPlanId() == null || level.getFee() == null || level.getStatus() == null
                || level.getDescription() == null || level.getDescription().trim().isEmpty())) {
            throw new ServiceException("等级名称、护理计划、费用、状态和等级说明不能为空");
        }
        if (level.getName() != null && (level.getName().trim().isEmpty()
                || level.getName().codePointCount(0, level.getName().length()) > 10
                || containsEmoji(level.getName()))) {
            throw new ServiceException("等级名称须为1到10个字符且不能包含表情");
        }
        if (level.getPlanId() != null) {
            NursingPlan plan = nursingPlanMapper.selectById(level.getPlanId());
            if (plan == null || !Integer.valueOf(1).equals(plan.getStatus())) {
                throw new ServiceException("请选择已启用的护理计划");
            }
        }
        if (level.getFee() != null && (level.getFee().compareTo(BigDecimal.ZERO) < 0
                || level.getFee().scale() > 2 || level.getFee().compareTo(new BigDecimal("99999999.99")) > 0)) {
            throw new ServiceException("护理费用须为非负且最多两位小数");
        }
        if (level.getStatus() != null && level.getStatus() != 0 && level.getStatus() != 1) {
            throw new ServiceException("护理等级状态只能为启用或禁用");
        }
        if (level.getDescription() != null && (level.getDescription().trim().isEmpty()
                || level.getDescription().codePointCount(0, level.getDescription().length()) > 50
                || containsEmoji(level.getDescription()))) {
            throw new ServiceException("等级说明须为1到50个字符且不能包含表情");
        }
        if (level.getName() != null) {
            level.setName(level.getName().trim());
            LambdaQueryWrapper<NursingLevel> sameName = new LambdaQueryWrapper<NursingLevel>()
                    .eq(NursingLevel::getName, level.getName());
            if (level.getId() != null) {
                sameName.ne(NursingLevel::getId, level.getId());
            }
            Long count = nursingLevelMapper.selectCount(sameName);
            if (count != null && count > 0) {
                throw new ServiceException("护理等级名称已存在");
            }
        }
    }

    private boolean containsEmoji(String value) {
        return value.codePoints().anyMatch(codePoint ->
                (codePoint >= 0x1F000 && codePoint <= 0x1FAFF)
                || (codePoint >= 0x2600 && codePoint <= 0x27BF)
                || codePoint == 0xFE0F || codePoint == 0x200D);
    }
}
