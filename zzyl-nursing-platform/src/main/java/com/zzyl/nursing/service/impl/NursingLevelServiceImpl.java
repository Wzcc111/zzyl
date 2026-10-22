package com.zzyl.nursing.service.impl;

import java.util.List;
        import com.zzyl.common.utils.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.zzyl.nursing.mapper.NursingLevelMapper;
import com.zzyl.nursing.domain.NursingLevel;
import com.zzyl.nursing.service.INursingLevelService;
import com.zzyl.nursing.vo.NursingLevelVo;
import com.zzyl.nursing.mapper.NursingPlanMapper;
import com.zzyl.common.exception.ServiceException;
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
                || level.getPlanId() == null || level.getFee() == null || level.getStatus() == null)) {
            throw new ServiceException("等级名称、护理计划、费用和状态不能为空");
        }
        if (level.getName() != null && (level.getName().trim().isEmpty() || level.getName().length() > 50)) {
            throw new ServiceException("等级名称长度须为1到50个字符");
        }
        if (level.getPlanId() != null && nursingPlanMapper.selectById(level.getPlanId()) == null) {
            throw new ServiceException("选择的护理计划不存在");
        }
        if (level.getFee() != null && (level.getFee().compareTo(BigDecimal.ZERO) < 0
                || level.getFee().scale() > 2 || level.getFee().compareTo(new BigDecimal("99999999.99")) > 0)) {
            throw new ServiceException("护理费用须为非负且最多两位小数");
        }
        if (level.getStatus() != null && level.getStatus() != 0 && level.getStatus() != 1) {
            throw new ServiceException("护理等级状态只能为启用或禁用");
        }
        if (level.getDescription() != null && level.getDescription().length() > 255) {
            throw new ServiceException("等级说明不能超过255个字符");
        }
    }
}
