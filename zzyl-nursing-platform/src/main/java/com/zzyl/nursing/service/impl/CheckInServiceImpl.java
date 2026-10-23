package com.zzyl.nursing.service.impl;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.time.LocalDateTime;

import cn.hutool.core.util.IdcardUtil;
import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zzyl.common.exception.ServiceException;
import com.zzyl.nursing.domain.Bed;
import com.zzyl.nursing.domain.CheckInConfig;
import com.zzyl.nursing.domain.Contract;
import com.zzyl.nursing.domain.Elder;
import com.zzyl.nursing.dto.CheckInApplyDto;
import com.zzyl.nursing.dto.CheckInElderDto;
import com.zzyl.nursing.mapper.BedMapper;
import com.zzyl.nursing.mapper.CheckInConfigMapper;
import com.zzyl.nursing.mapper.ContractMapper;
import com.zzyl.nursing.mapper.ElderMapper;
import com.zzyl.nursing.utils.ContractNumberGenerator;
import com.zzyl.nursing.vo.CheckInConfigVo;
import com.zzyl.nursing.vo.CheckInDetailVo;
import com.zzyl.nursing.vo.CheckInElderVo;
import com.zzyl.nursing.vo.ElderFamilyVo;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.zzyl.nursing.mapper.CheckInMapper;
import com.zzyl.nursing.domain.CheckIn;
import com.zzyl.nursing.service.ICheckInService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

/**
 * 入住Service业务层处理
 *
 * @author alexis
 * @date 2026-10-23
 */
@Service
public class CheckInServiceImpl extends ServiceImpl<CheckInMapper, CheckIn> implements ICheckInService
{
    @Autowired
    private CheckInMapper checkInMapper;

    @Autowired
    private ElderMapper elderMapper;

    @Autowired
    private BedMapper bedMapper;

    @Autowired
    private CheckInConfigMapper checkInConfigMapper;

    @Autowired
    private ContractMapper contractMapper;

    /**
     * 查询入住
     *
     * @param id 入住主键
     * @return 入住
     */
    @Override
    public CheckIn selectCheckInById(Long id)
    {
        return getById(id);
    }

    /**
     * 查询入住列表
     *
     * @param checkIn 入住
     * @return 入住
     */
    @Override
    public List<CheckIn> selectCheckInList(CheckIn checkIn)
    {
        return checkInMapper.selectCheckInList(checkIn);
    }

    /**
     * 新增入住
     *
     * @param checkIn 入住
     * @return 结果
     */
    @Override
    public int insertCheckIn(CheckIn checkIn)
    {
        return save(checkIn) ? 1 : 0;
    }

    /**
     * 修改入住
     *
     * @param checkIn 入住
     * @return 结果
     */
    @Override
    public int updateCheckIn(CheckIn checkIn)
    {
        return updateById(checkIn) ? 1 : 0;
    }

    /**
     * 批量删除入住
     *
     * @param ids 需要删除的入住主键
     * @return 结果
     */
    @Override
    public int deleteCheckInByIds(Long[] ids)
    {
        return removeByIds(Arrays.asList(ids)) ? 1 : 0;
    }

    /**
     * 删除入住信息
     *
     * @param id 入住主键
     * @return 结果
     */
    @Override
    public int deleteCheckInById(Long id)
    {
        return removeById(id) ? 1 : 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void apply(CheckInApplyDto request) {
        validateApplyRequest(request);

        CheckInElderDto elderDto = request.getCheckInElderDto();
        Elder existing = elderMapper.selectOne(new LambdaQueryWrapper<Elder>()
                .eq(Elder::getIdCardNo, elderDto.getIdCardNo()));
        if (existing != null && (Integer.valueOf(1).equals(existing.getStatus())
                || Integer.valueOf(4).equals(existing.getStatus()))) {
            throw new ServiceException("老人已入住");
        }

        Bed bed = bedMapper.selectById(request.getCheckInConfigDto().getBedId());
        if (bed == null || !Integer.valueOf(0).equals(bed.getBedStatus())) {
            throw new ServiceException("床位不存在或已被占用");
        }
        bed.setBedStatus(1);
        bedMapper.updateById(bed);

        Elder elder = saveOrUpdateElder(existing, bed, elderDto);
        insertContract(elder, request);
        CheckIn checkIn = insertAdmission(elder, request);
        insertAdmissionConfig(checkIn.getId(), request);
    }

    @Override
    public CheckInDetailVo detail(Long id) {
        CheckIn checkIn = checkInMapper.selectCheckInById(id);
        if (checkIn == null) {
            throw new ServiceException("入住记录不存在");
        }

        Elder elder = elderMapper.selectById(checkIn.getElderId());
        if (elder == null) {
            throw new ServiceException("入住记录关联的老人不存在");
        }

        CheckInConfig config = checkInConfigMapper.selectOne(new LambdaQueryWrapper<CheckInConfig>()
                .eq(CheckInConfig::getCheckInId, id));
        Contract contract = contractMapper.selectOne(new LambdaQueryWrapper<Contract>()
                .eq(Contract::getElderId, checkIn.getElderId())
                .orderByDesc(Contract::getId)
                .last("limit 1"));

        CheckInElderVo elderVo = new CheckInElderVo();
        BeanUtils.copyProperties(elder, elderVo);
        if (checkIn.getElderName() != null && !checkIn.getElderName().trim().isEmpty()) {
            elderVo.setName(checkIn.getElderName());
        }
        elderVo.setAge(elder.getIdCardNo() == null ? null
                : IdcardUtil.getAgeByIdCard(elder.getIdCardNo()));

        CheckInConfigVo configVo = new CheckInConfigVo();
        if (config != null) {
            BeanUtils.copyProperties(config, configVo);
        }
        configVo.setStartDate(checkIn.getStartDate());
        configVo.setEndDate(checkIn.getEndDate());
        configVo.setBedNumber(checkIn.getBedNumber());

        List<ElderFamilyVo> family = Collections.emptyList();
        if (checkIn.getRemark() != null && !checkIn.getRemark().trim().isEmpty()) {
            family = JSON.parseArray(checkIn.getRemark(), ElderFamilyVo.class);
            if (family == null) {
                family = Collections.emptyList();
            }
        }

        CheckInDetailVo result = new CheckInDetailVo();
        result.setCheckInElderVo(elderVo);
        result.setElderFamilyVoList(family);
        result.setCheckInConfigVo(configVo);
        result.setContract(contract);
        return result;
    }

    private void validateApplyRequest(CheckInApplyDto request) {
        if (request == null || request.getCheckInElderDto() == null
                || request.getCheckInConfigDto() == null || request.getCheckInContractDto() == null) {
            throw new ServiceException("申请入住信息不完整");
        }
        if (request.getCheckInElderDto().getIdCardNo() == null
                || request.getCheckInConfigDto().getBedId() == null) {
            throw new ServiceException("身份证号和床位编号不能为空");
        }
        if (request.getCheckInContractDto().getAgreementPath() == null
                || request.getCheckInContractDto().getAgreementPath().trim().isEmpty()) {
            throw new ServiceException("请先上传入住合同");
        }
    }

    private Elder saveOrUpdateElder(Elder existing, Bed bed, CheckInElderDto elderDto) {
        Elder elder = new Elder();
        BeanUtils.copyProperties(elderDto, elder);
        elder.setBedId(bed.getId());
        elder.setBedNumber(bed.getBedNumber());
        elder.setStatus(1);
        if (existing == null) {
            elderMapper.insert(elder);
        } else {
            elder.setId(existing.getId());
            elderMapper.updateById(elder);
        }
        return elder;
    }

    private void insertContract(Elder elder, CheckInApplyDto request) {
        Contract contract = new Contract();
        BeanUtils.copyProperties(request.getCheckInContractDto(), contract);
        contract.setContractNumber(ContractNumberGenerator.next());
        contract.setElderId(elder.getId());
        contract.setElderName(elder.getName());
        LocalDateTime startDate = request.getCheckInConfigDto().getStartDate();
        contract.setStartDate(startDate);
        contract.setEndDate(request.getCheckInConfigDto().getEndDate());
        contract.setStatus(startDate != null && startDate.isAfter(LocalDateTime.now()) ? 0 : 1);
        contractMapper.insert(contract);
    }

    private CheckIn insertAdmission(Elder elder, CheckInApplyDto request) {
        CheckIn checkIn = new CheckIn();
        checkIn.setElderId(elder.getId());
        checkIn.setElderName(elder.getName());
        checkIn.setIdCardNo(elder.getIdCardNo());
        checkIn.setNursingLevelName(request.getCheckInConfigDto().getNursingLevelName());
        checkIn.setStartDate(request.getCheckInConfigDto().getStartDate());
        checkIn.setEndDate(request.getCheckInConfigDto().getEndDate());
        checkIn.setBedNumber(elder.getBedNumber());
        checkIn.setRemark(JSON.toJSONString(request.getElderFamilyDtoList() == null
                ? Collections.emptyList() : request.getElderFamilyDtoList()));
        checkIn.setStatus(0);
        checkInMapper.insert(checkIn);
        return checkIn;
    }

    private void insertAdmissionConfig(Long checkInId, CheckInApplyDto request) {
        CheckInConfig config = new CheckInConfig();
        BeanUtils.copyProperties(request.getCheckInConfigDto(), config);
        config.setCheckInId(checkInId);
        checkInConfigMapper.insert(config);
    }

}
